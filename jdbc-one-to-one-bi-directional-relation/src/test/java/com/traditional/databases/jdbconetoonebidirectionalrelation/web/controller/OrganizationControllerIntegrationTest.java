package com.traditional.databases.jdbconetoonebidirectionalrelation.web.controller;

import com.traditional.databases.jdbconetoonebidirectionalrelation.db.repository.AddressRepository;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.repository.OrganizationRepository;
import com.traditional.databases.jdbconetoonebidirectionalrelation.web.model.request.AddressRequest;
import com.traditional.databases.jdbconetoonebidirectionalrelation.web.model.request.OrganizationRequest;
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

    @Test
    void enterpriseHeadquartersLifecycle_shouldCreateUpdateRetrieveAndDeleteOrganization() {
        OrganizationRequest createRequest = new OrganizationRequest(
                "Northwind Systems",
                "ORG-9001",
                new AddressRequest("HQ-1", "River Road", "Denver", "Colorado", "USA", "80202")
        );

        AtomicLong organizationId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/organization/create")
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> organizationId.set(((Number) id).longValue()))
                .jsonPath("$.orgId").isEqualTo("ORG-9001")
                .jsonPath("$.address.city").isEqualTo("Denver");

        webTestClient.get()
                .uri("/api/v1/organization/get/{organizationId}", organizationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Northwind Systems")
                .jsonPath("$.address.street").isEqualTo("River Road");

        OrganizationRequest updateRequest = new OrganizationRequest(
                "Northwind Systems",
                "ORG-9001",
                new AddressRequest("HQ-2", "Innovation Ave", "Seattle", "Washington", "USA", "98101")
        );

        webTestClient.put()
                .uri("/api/v1/organization/update/{organizationId}", organizationId.get())
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(organizationId.get())
                .jsonPath("$.address.building").isEqualTo("HQ-2")
                .jsonPath("$.address.city").isEqualTo("Seattle");

        webTestClient.get()
                .uri("/api/v1/organization/get/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(organizationId.get())
                .jsonPath("$[0].address.street").isEqualTo("Innovation Ave");

        webTestClient.delete()
                .uri("/api/v1/organization/delete/{organizationId}", organizationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(organizationId.get());

        webTestClient.get()
                .uri("/api/v1/organization/get/{organizationId}", organizationId.get())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Organization not found with Id: " + organizationId.get());

        assertThat(organizationRepository.count()).isZero();
        assertThat(addressRepository.count()).isZero();
    }

    @Test
    void updatingNonExistentOrganization_shouldReturnNotFound() {
        OrganizationRequest updateRequest = new OrganizationRequest(
                "Ghost Org",
                "ORG-404",
                new AddressRequest("X", "Y", "Z", "Z", "Z", "00000")
        );

        webTestClient.put()
                .uri("/api/v1/organization/update/{organizationId}", 999999L)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Organization not found with Id: 999999");
    }

    @Test
    void updatingOrganization_withInvalidAddress_shouldNotMutatePersistedRecord() {
        OrganizationRequest createRequest = new OrganizationRequest(
                "Helios Labs",
                "ORG-4200",
                new AddressRequest("Lab-1", "Science Park", "Boston", "Massachusetts", "USA", "02108")
        );

        AtomicLong organizationId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/organization/create")
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> organizationId.set(((Number) id).longValue()));

        webTestClient.put()
                .uri("/api/v1/organization/update/{organizationId}", organizationId.get())
                .bodyValue(new OrganizationRequest(
                        "Helios Labs",
                        "ORG-4200",
                        new AddressRequest("", "Science Park", "Boston", "Massachusetts", "USA", "02108")
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("address.building is required");

        webTestClient.get()
                .uri("/api/v1/organization/get/{organizationId}", organizationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Helios Labs")
                .jsonPath("$.orgId").isEqualTo("ORG-4200")
                .jsonPath("$.address.building").isEqualTo("Lab-1");
    }

    @Test
    void deletingNonExistentOrganization_shouldReturnNotFoundAndKeepExistingRecordsUnchanged() {
        OrganizationRequest createRequest = new OrganizationRequest(
                "Vector Dynamics",
                "ORG-7777",
                new AddressRequest("Campus-7", "Orbit Street", "Phoenix", "Arizona", "USA", "85001")
        );

        AtomicLong existingOrganizationId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/organization/create")
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> existingOrganizationId.set(((Number) id).longValue()));

        webTestClient.delete()
                .uri("/api/v1/organization/delete/{organizationId}", 424242L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Organization not found with Id: 424242");

        assertThat(organizationRepository.count()).isEqualTo(1L);
        assertThat(addressRepository.count()).isEqualTo(1L);

        webTestClient.get()
                .uri("/api/v1/organization/get/{organizationId}", existingOrganizationId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Vector Dynamics")
                .jsonPath("$.address.building").isEqualTo("Campus-7");
    }

    @Test
    void deletingOrganization_shouldCascadeDeleteAddressRecord() {
        OrganizationRequest createRequest = new OrganizationRequest(
                "Cascade Labs",
                "ORG-8800",
                new AddressRequest("Tower-C", "Cascade Lane", "San Jose", "California", "USA", "95112")
        );

        AtomicLong organizationId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/organization/create")
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> organizationId.set(((Number) id).longValue()));

        assertThat(organizationRepository.count()).isEqualTo(1L);
        assertThat(addressRepository.count()).isEqualTo(1L);

        webTestClient.delete()
                .uri("/api/v1/organization/delete/{organizationId}", organizationId.get())
                .exchange()
                .expectStatus().isOk();

        assertThat(organizationRepository.count()).isZero();
        assertThat(addressRepository.count()).isZero();
    }
}

