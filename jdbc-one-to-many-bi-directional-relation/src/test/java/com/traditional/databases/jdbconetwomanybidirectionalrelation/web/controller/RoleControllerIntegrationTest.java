package com.traditional.databases.jdbconetwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.RoleRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository.UserRepository;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.UserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RoleControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setupClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void createRole_withNestedUsers_shouldPersistAssociationAndReturnCreated() {
        RoleRequest request = new RoleRequest(
                "Platform Engineer",
                "Platform role with two users",
                List.of(
                        new UserRequest("Ava", "Miller", "1234567890", "ava@example.com"),
                        new UserRequest("Noah", "Brown", "9988776655", "noah@example.com")
                )
        );

        webTestClient.post()
                .uri("/api/v1/role/create")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.name").isEqualTo("Platform Engineer")
                .jsonPath("$.users.length()").isEqualTo(2)
                .jsonPath("$.users[0].roleId").isNumber();
    }

    @Test
    void createRole_withoutName_shouldReturnBadRequest() {
        webTestClient.post()
                .uri("/api/v1/role/create")
                .bodyValue(Map.of("description", "Missing role name"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("name is required");
    }
}

