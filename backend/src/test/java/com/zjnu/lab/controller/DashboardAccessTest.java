package com.zjnu.lab.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:dashboardSecurity;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
)
@ActiveProfiles("dev")
class DashboardAccessTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void dashboardIsRestrictedToManagementRoles() {
        assertThat(getDashboardStatus("student")).isEqualTo(403);
        assertThat(getDashboardStatus("labadmin1")).isEqualTo(200);
        assertThat(getDashboardStatus("admin")).isEqualTo(200);
    }

    private int getDashboardStatus(String username) {
        ResponseEntity<Map> login = rest.postForEntity(
                url("/auth/login"),
                Map.of("username", username, "password", "123456"),
                Map.class
        );
        assertThat(login.getStatusCode().value()).isEqualTo(200);
        Map<?, ?> data = (Map<?, ?>) login.getBody().get("data");
        String token = (String) data.get("token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return rest.exchange(url("/dashboard"), HttpMethod.GET, new HttpEntity<>(headers), Map.class)
                .getStatusCode()
                .value();
    }

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }
}
