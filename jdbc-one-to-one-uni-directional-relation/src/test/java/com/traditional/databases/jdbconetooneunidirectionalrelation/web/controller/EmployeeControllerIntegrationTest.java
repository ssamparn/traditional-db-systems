package com.traditional.databases.jdbconetooneunidirectionalrelation.web.controller;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository.EmployeeRepository;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository.WorkstationRepository;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.EmployeeRequest;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.WorkstationRequest;
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
class EmployeeControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkstationRepository workstationRepository;

    @BeforeEach
    void setupClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanup() {
        employeeRepository.deleteAll();
        workstationRepository.deleteAll();
    }

    @Test
    void createEmployee_shouldPersistAndReturnCreated() {
        EmployeeRequest request = new EmployeeRequest(
                "EMP-1001",
                "Ava Peterson",
                new WorkstationRequest("D-42", "HQ-North", 6, "Platform")
        );

        webTestClient.post()
                .uri("/api/v1/employee/create")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.employeeCode").isEqualTo("EMP-1001")
                .jsonPath("$.fullName").isEqualTo("Ava Peterson")
                .jsonPath("$.workstation.deskCode").isEqualTo("D-42");
    }

    @Test
    void createEmployee_withoutWorkstation_shouldReturnBadRequest() {
        webTestClient.post()
                .uri("/api/v1/employee/create")
                .bodyValue(Map.of(
                        "employeeCode", "EMP-1002",
                        "fullName", "No Workstation"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("workstation is required");
    }
}

