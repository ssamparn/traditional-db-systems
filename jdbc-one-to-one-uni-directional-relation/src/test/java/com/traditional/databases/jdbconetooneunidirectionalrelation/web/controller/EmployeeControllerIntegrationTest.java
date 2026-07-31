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
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void getEmployeeById_shouldReturnPersistedEmployeeAndWorkstation() {
        AtomicLong employeeId = createEmployeeAndCaptureId("EMP-1003", "Riya Singh", "D-50", "HQ-East", 2, "Ops");

        webTestClient.get()
                .uri("/api/v1/employee/get/{employeeId}", employeeId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(employeeId.get())
                .jsonPath("$.employeeCode").isEqualTo("EMP-1003")
                .jsonPath("$.workstation.deskCode").isEqualTo("D-50");
    }

    @Test
    void getAllEmployees_shouldReturnAllPersistedRecords() {
        createEmployeeAndCaptureId("EMP-1004", "Noah Walker", "D-51", "HQ-West", 5, "Platform");
        createEmployeeAndCaptureId("EMP-1005", "Mia Carter", "D-52", "HQ-West", 6, "Platform");

        webTestClient.get()
                .uri("/api/v1/employee/get/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isNumber()
                .jsonPath("$[1].id").isNumber();

        assertThat(employeeRepository.count()).isEqualTo(2L);
        assertThat(workstationRepository.count()).isEqualTo(2L);
    }

    @Test
    void updateEmployee_shouldReplaceWorkstationAndDeleteOldWorkstation() {
        AtomicLong employeeId = new AtomicLong();
        AtomicLong oldWorkstationId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/employee/create")
                .bodyValue(new EmployeeRequest("EMP-1006", "Aria Holt", new WorkstationRequest("D-53", "HQ-North", 7, "Core")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> employeeId.set(((Number) id).longValue()))
                .jsonPath("$.workstation.id").value(id -> oldWorkstationId.set(((Number) id).longValue()));

        webTestClient.put()
                .uri("/api/v1/employee/update/{employeeId}", employeeId.get())
                .bodyValue(new EmployeeRequest("EMP-1006", "Aria Holt Updated", new WorkstationRequest("D-99", "HQ-South", 10, "Security")))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(employeeId.get())
                .jsonPath("$.fullName").isEqualTo("Aria Holt Updated")
                .jsonPath("$.workstation.deskCode").isEqualTo("D-99");

        assertThat(workstationRepository.existsById(oldWorkstationId.get())).isFalse();
        assertThat(employeeRepository.findById(employeeId.get()).orElseThrow().getWorkstation().getDeskCode()).isEqualTo("D-99");
    }

    @Test
    void deleteEmployee_shouldDeleteEmployeeAndCascadeDeleteWorkstation() {
        AtomicLong employeeId = new AtomicLong();
        AtomicLong workstationId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/employee/create")
                .bodyValue(new EmployeeRequest("EMP-1007", "Luca Hall", new WorkstationRequest("D-54", "HQ-Central", 1, "Infra")))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> employeeId.set(((Number) id).longValue()))
                .jsonPath("$.workstation.id").value(id -> workstationId.set(((Number) id).longValue()));

        webTestClient.delete()
                .uri("/api/v1/employee/delete/{employeeId}", employeeId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(employeeId.get());

        assertThat(employeeRepository.existsById(employeeId.get())).isFalse();
        assertThat(workstationRepository.existsById(workstationId.get())).isFalse();
    }

    @Test
    void getEmployee_withUnknownId_shouldReturnNotFound() {
        webTestClient.get()
                .uri("/api/v1/employee/get/{employeeId}", 999999L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Employee not found with Id: 999999");
    }

    @Test
    void updateEmployee_withUnknownId_shouldReturnNotFound() {
        webTestClient.put()
                .uri("/api/v1/employee/update/{employeeId}", 999998L)
                .bodyValue(new EmployeeRequest("EMP-404", "Missing User", new WorkstationRequest("D-404", "NA", 4, "NA")))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Employee not found with Id: 999998");
    }

    @Test
    void deleteEmployee_withUnknownId_shouldReturnNotFound() {
        webTestClient.delete()
                .uri("/api/v1/employee/delete/{employeeId}", 999997L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Employee not found with Id: 999997");
    }

    @Test
    void createEmployee_withOutOfRangeFloorNumber_shouldReturnBadRequest() {
        webTestClient.post()
                .uri("/api/v1/employee/create")
                .bodyValue(new EmployeeRequest("EMP-1008", "Invalid Floor", new WorkstationRequest("D-55", "HQ-Edge", 201, "Ops")))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("workstation.floorNumber must be between 0 and 200");
    }

    @Test
    void updateEmployee_withBlankEmployeeCode_shouldReturnBadRequestAndKeepRecordUnchanged() {
        AtomicLong employeeId = createEmployeeAndCaptureId("EMP-1009", "Stable Employee", "D-56", "HQ-Stable", 11, "Stable");

        webTestClient.put()
                .uri("/api/v1/employee/update/{employeeId}", employeeId.get())
                .bodyValue(new EmployeeRequest("", "Mutated Name", new WorkstationRequest("D-57", "HQ-Stable", 12, "Stable")))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("employeeCode is required");

        webTestClient.get()
                .uri("/api/v1/employee/get/{employeeId}", employeeId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.employeeCode").isEqualTo("EMP-1009")
                .jsonPath("$.fullName").isEqualTo("Stable Employee")
                .jsonPath("$.workstation.deskCode").isEqualTo("D-56");
    }

    private AtomicLong createEmployeeAndCaptureId(String employeeCode,
                                                  String fullName,
                                                  String deskCode,
                                                  String building,
                                                  Integer floorNumber,
                                                  String zone) {
        AtomicLong employeeId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/employee/create")
                .bodyValue(new EmployeeRequest(employeeCode, fullName, new WorkstationRequest(deskCode, building, floorNumber, zone)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> employeeId.set(((Number) id).longValue()));

        return employeeId;
    }

}

