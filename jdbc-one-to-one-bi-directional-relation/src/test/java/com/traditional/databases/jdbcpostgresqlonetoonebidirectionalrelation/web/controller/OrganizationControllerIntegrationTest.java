package com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.web.controller;

import com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.db.repository.AddressRepository;
import com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.db.repository.OrganizationRepository;
import com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.web.model.request.AddressRequest;
import com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.web.model.request.OrganizationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrganizationControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AddressRepository addressRepository;

    @BeforeEach
    void setupClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanup() {
        organizationRepository.deleteAll();
        addressRepository.deleteAll();
    }

    @Test
    void createOrganization_shouldPersistAndReturnCreated() {
        OrganizationRequest request = new OrganizationRequest(
                "Acme Inc",
                "ORG-001",
                new AddressRequest("Tower A", "Main Street", "Austin", "Texas", "USA", "73301")
        );

        webTestClient.post()
                .uri("/api/v1/organization/create")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.name").isEqualTo("Acme Inc")
                .jsonPath("$.orgId").isEqualTo("ORG-001")
                .jsonPath("$.address.city").isEqualTo("Austin");
    }

    @Test
    void createOrganization_withoutAddress_shouldReturnBadRequest() {
        webTestClient.post()
                .uri("/api/v1/organization/create")
                .bodyValue(Map.of(
                        "organizationName", "Acme Inc",
                        "organizationId", "ORG-001"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("address is required");
    }
}

