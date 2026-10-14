package com.g7.crowdfunding.bootstrap;

import com.g7.crowdfunding.campaign.application.port.in.CreateCampaignUseCase;
import com.g7.crowdfunding.campaign.application.port.in.CreateCampaignUseCase.CreateCampaignCommand;
import com.g7.crowdfunding.campaign.application.port.in.ManageCampaignLifecycleUseCase;
import com.g7.crowdfunding.campaign.domain.model.Category;
import com.g7.crowdfunding.campaign.domain.model.ContributorsVisibility;
import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase;
import com.g7.crowdfunding.contribution.application.port.in.ContributeToCampaignUseCase.ContributeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Seeds demo campaigns through the real use cases (so progress, milestones and outbox events
 * are consistent). Users are seeded by Flyway. Runs only when the campaigns table is empty.
 */
@Component
@ConditionalOnProperty(name = "crowdfunding.seed.enabled", havingValue = "true")
class DemoDataSeeder implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDataSeeder.class);

    static final UUID ANA = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID DIEGO = UUID.fromString("44444444-4444-4444-4444-444444444444");
    static final UUID BRUNO = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID CARLA = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private final CreateCampaignUseCase create;
    private final ManageCampaignLifecycleUseCase lifecycle;
    private final ContributeToCampaignUseCase contribute;
    private final JdbcTemplate jdbc;

    DemoDataSeeder(CreateCampaignUseCase create, ManageCampaignLifecycleUseCase lifecycle,
                   ContributeToCampaignUseCase contribute, JdbcTemplate jdbc) {
        this.create = create;
        this.lifecycle = lifecycle;
        this.contribute = contribute;
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long existing = jdbc.queryForObject("select count(*) from campaigns", Long.class);
        if (existing != null && existing > 0) {
            return;
        }
        Instant now = Instant.now();

        UUID garden = active(ANA, "Huerta comunitaria en el barrio",
                "Recuperamos un terreno baldío para convertirlo en una huerta abierta a todos los vecinos, con talleres de compostaje y riego eficiente.",
                Category.COMMUNITY, "5000", now.plus(Duration.ofDays(20)), ContributorsVisibility.PUBLIC);
        give(garden, BRUNO, "1200", false, "¡Vamos por esa huerta!");
        give(garden, CARLA, "800", true, null);
        give(garden, DIEGO, "500", false, "Cuenten conmigo");

        UUID printer = active(ANA, "Impresora 3D para la escuela técnica",
                "Queremos equipar el laboratorio de la escuela técnica con una impresora 3D para proyectos de los estudiantes.",
                Category.EDUCATION, "1500", now.plus(Duration.ofDays(10)), ContributorsVisibility.PUBLIC);
        give(printer, BRUNO, "900", false, "Para los futuros ingenieros");
        give(printer, CARLA, "700", false, null); // reaches the goal -> GoalReached

        UUID documentary = active(DIEGO, "Documental sobre el río",
                "Un documental independiente sobre la vida alrededor del río y los desafíos ambientales de la región.",
                Category.ART, "8000", now.plus(Duration.ofHours(20)), ContributorsVisibility.PRIVATE);
        give(documentary, BRUNO, "300", false, null); // closes within 24h -> ClosingSoon

        active(DIEGO, "App de turnos para centros de salud",
                "Una aplicación gratuita para pedir turnos en los centros de atención primaria del municipio.",
                Category.HEALTH, "12000", now.plus(Duration.ofDays(45)), ContributorsVisibility.PUBLIC);

        UUID robotics = active(ANA, "Taller de robótica para niñas",
                "Un taller de robótica de ocho semanas para niñas de 10 a 14 años. Esta campaña cierra en pocos minutos para demostrar el cierre automático.",
                Category.TECHNOLOGY, "3000", now.plus(Duration.ofMinutes(3)), ContributorsVisibility.PUBLIC);
        give(robotics, CARLA, "250", false, "¡Éxitos!");

        create.create(new CreateCampaignCommand(ANA, "Biblioteca itinerante",
                "Una biblioteca sobre ruedas que recorre los barrios los fines de semana.", Category.EDUCATION,
                new BigDecimal("2500"), "USD", now.plus(Duration.ofDays(30)), ContributorsVisibility.PUBLIC));

        UUID solar = active(DIEGO, "Paneles solares para el club",
                "Instalación de paneles solares en el club del barrio para reducir su factura eléctrica.",
                Category.ENVIRONMENT, "2000", now.plus(Duration.ofDays(15)), ContributorsVisibility.PUBLIC);
        give(solar, BRUNO, "2000", false, "Energía limpia para todos");
        lifecycle.closeManually(solar, DIEGO);

        UUID concert = active(ANA, "Concierto benéfico",
                "Un concierto a beneficio del comedor comunitario.", Category.ART, "1000",
                now.plus(Duration.ofDays(12)), ContributorsVisibility.PUBLIC);
        lifecycle.cancel(concert, ANA);

        LOG.info("Demo campaigns seeded");
    }

    private UUID active(UUID creator, String title, String description, Category category, String goal,
                        Instant deadline, ContributorsVisibility visibility) {
        UUID id = create.create(new CreateCampaignCommand(creator, title, description, category,
                new BigDecimal(goal), "USD", deadline, visibility));
        lifecycle.publish(id, creator);
        return id;
    }

    private void give(UUID campaign, UUID contributor, String amount, boolean anonymous, String message) {
        contribute.contribute(new ContributeCommand(campaign, contributor, new BigDecimal(amount), "USD",
                anonymous, message, UUID.randomUUID()));
    }
}
