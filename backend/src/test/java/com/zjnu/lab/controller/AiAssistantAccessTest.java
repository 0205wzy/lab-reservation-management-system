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
        properties = "spring.datasource.url=jdbc:h2:mem:aiAssistantSecurity;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
)
@ActiveProfiles("dev")
class AiAssistantAccessTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void endpointRequiresLoginAndStudentRoleAndProvidesLocalAnswer() {
        assertThat(ask(null, "实验室预约需要注意什么？").getStatusCode().value()).isEqualTo(401);
        assertThat(ask(login("labadmin1"), "实验室预约需要注意什么？").getStatusCode().value()).isEqualTo(403);
        assertThat(ask(login("admin"), "实验室预约需要注意什么？").getStatusCode().value()).isEqualTo(403);

        ResponseEntity<Map> response = ask(login("student"), "实验室预约需要注意什么？");
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        assertThat(data.get("source")).isEqualTo("LOCAL");
        assertThat(data.get("answer").toString()).contains("待审核");
    }

    @Test
    void endpointValidatesEmptyAndOversizedQuestions() {
        HttpHeaders headers = bearer(login("student"));

        ResponseEntity<Map> empty = rest.exchange(
                url(), HttpMethod.POST, new HttpEntity<>(Map.of("question", "  "), headers), Map.class
        );
        ResponseEntity<Map> tooLong = rest.exchange(
                url(), HttpMethod.POST, new HttpEntity<>(Map.of("question", "问".repeat(501)), headers), Map.class
        );

        assertThat(empty.getStatusCode().value()).isEqualTo(400);
        assertThat(tooLong.getStatusCode().value()).isEqualTo(400);
    }

    private ResponseEntity<Map> ask(String token, String question) {
        HttpHeaders headers = token == null ? new HttpHeaders() : bearer(token);
        return rest.exchange(url(), HttpMethod.POST, new HttpEntity<>(Map.of("question", question), headers), Map.class);
    }

    private String login(String username) {
        ResponseEntity<Map> response = rest.postForEntity(
                "http://localhost:" + port + "/api/auth/login",
                Map.of("username", username, "password", "123456"),
                Map.class
        );
        Map<?, ?> data = (Map<?, ?>) response.getBody().get("data");
        return data.get("token").toString();
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String url() {
        return "http://localhost:" + port + "/api/ai/ask";
    }
}
