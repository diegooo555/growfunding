package com.g7.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GatewayRoutesTest {

    @Autowired
    private RouteLocator routes;

    @Test
    void statsRouteIsCheckedBeforeTheCatchAllCoreRoute() {
        List<Route> all = routes.getRoutes().collectList().block();

        assertThat(all).extracting(Route::getId).containsExactly("stats-route", "core-route");
        assertThat(all.get(0).getUri().getPort()).isEqualTo(8082);
        assertThat(all.get(1).getUri().getPort()).isEqualTo(8081);
    }
}
