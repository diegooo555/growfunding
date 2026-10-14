package com.g7.crowdfunding.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g7.crowdfunding.shared.infrastructure.outbox.OutboxEventJpaRepository;
import com.g7.crowdfunding.shared.infrastructure.outbox.OutboxRelay;
import com.g7.crowdfunding.shared.infrastructure.outbox.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "crowdfunding.seed.enabled=false",
        "crowdfunding.scheduling.enabled=false",
        "crowdfunding.outbox.dispatch-after-commit=false",
        "crowdfunding.contribution.max-attempts=30"
})
@AutoConfigureMockMvc
class ContributionFlowIntegrationTest {

    private static final String CREATOR = "11111111-1111-1111-1111-111111111111";
    private static final String BRUNO = "22222222-2222-2222-2222-222222222222";
    private static final String CARLA = "33333333-3333-3333-3333-333333333333";

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    OutboxRelay relay;
    @Autowired
    OutboxEventJpaRepository outbox;

    private String createActiveCampaign(String goal, String visibility) throws Exception {
        String body = """
                {"title":"Integration campaign","description":"Testing the full flow","category":"TECHNOLOGY",
                 "goalAmount":%s,"deadline":"%s","contributorsVisibility":"%s"}
                """.formatted(goal, Instant.now().plus(7, ChronoUnit.DAYS), visibility);
        MvcResult created = mvc.perform(post("/api/campaigns").header("X-User-Id", CREATOR)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();
        String id = json.readTree(created.getResponse().getContentAsString()).get("id").asText();
        mvc.perform(post("/api/campaigns/{id}/publish", id).header("X-User-Id", CREATOR))
                .andExpect(status().isNoContent());
        return id;
    }

    private MvcResult contribute(String campaignId, String userId, String amount, UUID key) throws Exception {
        String body = """
                {"amount":%s,"anonymous":false,"message":"good luck","idempotencyKey":"%s"}
                """.formatted(amount, key);
        return mvc.perform(post("/api/campaigns/{id}/contributions", campaignId).header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andReturn();
    }

    @Test
    void contributionFlowIsIdempotentAndNotifiesContributorsWhenGoalIsReached() throws Exception {
        String campaignId = createActiveCampaign("1000", "PUBLIC");
        UUID key = UUID.randomUUID();

        MvcResult first = contribute(campaignId, BRUNO, "600", key);
        MvcResult replay = contribute(campaignId, BRUNO, "600", key);

        assertThat(first.getResponse().getStatus()).isEqualTo(201);
        assertThat(replay.getResponse().getStatus()).isEqualTo(200);
        JsonNode firstBody = json.readTree(first.getResponse().getContentAsString());
        JsonNode replayBody = json.readTree(replay.getResponse().getContentAsString());
        assertThat(replayBody.get("contributionId")).isEqualTo(firstBody.get("contributionId"));
        assertThat(replayBody.get("replayed").asBoolean()).isTrue();

        assertThat(contribute(campaignId, CARLA, "500", UUID.randomUUID()).getResponse().getStatus()).isEqualTo(201);
        assertThat(contribute(campaignId, CARLA, "0", UUID.randomUUID()).getResponse().getStatus()).isEqualTo(422);

        mvc.perform(get("/api/campaigns/{id}", campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.raisedAmount").value(1100.0))
                .andExpect(jsonPath("$.summary.contributionsCount").value(2))
                .andExpect(jsonPath("$.summary.contributorsCount").value(2))
                .andExpect(jsonPath("$.summary.goalReached").value(true))
                .andExpect(jsonPath("$.milestones[?(@.type == 'GOAL_REACHED')]").exists());

        relay.dispatchPending();
        assertThat(outbox.countByStatus(OutboxStatus.PENDING)).isZero();

        for (String user : List.of(BRUNO, CARLA, CREATOR)) {
            mvc.perform(get("/api/users/{id}/notifications", user).header("X-User-Id", user))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.type == 'GOAL_REACHED' && @.campaignId == '" + campaignId + "')]")
                            .exists());
        }

        mvc.perform(get("/api/campaigns/{id}/contributors", campaignId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/contributions/me").header("X-User-Id", BRUNO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.campaignId == '" + campaignId + "')]").exists());

        mvc.perform(post("/api/campaigns/{id}/cancel", campaignId).header("X-User-Id", CREATOR))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CAMPAIGN_HAS_CONTRIBUTIONS"));
    }

    @Test
    void privateContributorsAreHidden() throws Exception {
        String campaignId = createActiveCampaign("100", "PRIVATE");
        mvc.perform(get("/api/campaigns/{id}/contributors", campaignId).header("X-User-Id", BRUNO))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/campaigns/{id}/contributors", campaignId).header("X-User-Id", CREATOR))
                .andExpect(status().isOk());
    }

    @Test
    void concurrentContributionsAreNeitherLostNorDuplicated() throws Exception {
        String campaignId = createActiveCampaign("100000", "PUBLIC");
        int requests = 12;
        ExecutorService pool = Executors.newFixedThreadPool(6);
        try {
            List<Callable<Integer>> tasks = new ArrayList<>();
            for (int i = 0; i < requests; i++) {
                String user = i % 2 == 0 ? BRUNO : CARLA;
                tasks.add(() -> contribute(campaignId, user, "10", UUID.randomUUID()).getResponse().getStatus());
            }
            for (Future<Integer> result : pool.invokeAll(tasks)) {
                assertThat(result.get()).isEqualTo(201);
            }
        } finally {
            pool.shutdown();
        }
        mvc.perform(get("/api/campaigns/{id}", campaignId))
                .andExpect(jsonPath("$.summary.raisedAmount").value(120.0))
                .andExpect(jsonPath("$.summary.contributionsCount").value(requests))
                .andExpect(jsonPath("$.summary.contributorsCount").value(2));
    }
}
