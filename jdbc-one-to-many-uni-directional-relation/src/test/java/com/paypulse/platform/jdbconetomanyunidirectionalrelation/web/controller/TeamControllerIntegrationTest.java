package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.controller;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.MemberRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.TeamRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.TeamRequest;
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
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TeamControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setupClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanup() {
        memberRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void createTeam_withMembers_shouldPersistAndReturnCreated() {
        TeamRequest request = new TeamRequest(
                "TEAM-PLATFORM",
                "Platform Team",
                "Builds and operates platform systems",
                List.of(
                        new MemberRequest("Ava", "Miller", "ava.team@example.com", "9090000001"),
                        new MemberRequest("Noah", "Brown", "noah.team@example.com", "9090000002")
                )
        );

        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.teamCode").isEqualTo("TEAM-PLATFORM")
                .jsonPath("$.members.length()").isEqualTo(2);
    }

    @Test
    void createTeam_withoutTeamCode_shouldReturnBadRequest() {
        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(Map.of(
                        "name", "No Code Team",
                        "description", "Invalid request"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("teamCode is required");
    }

    @Test
    void deleteTeam_shouldCascadeDeleteMembers() {
        TeamRequest request = new TeamRequest(
                "TEAM-OPS",
                "Operations Team",
                "Runs production operations",
                List.of(new MemberRequest("Mia", "Stone", "mia.ops@example.com", "9090000003"))
        );

        Long[] teamIdHolder = new Long[1];
        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> teamIdHolder[0] = ((Number) id).longValue());

        webTestClient.delete()
                .uri("/api/v1/team/delete/{teamId}", teamIdHolder[0])
                .exchange()
                .expectStatus().isOk();

        assertThat(teamRepository.count()).isZero();
        assertThat(memberRepository.count()).isZero();
    }

    @Test
    void getTeamById_shouldReturnPersistedTeamWithMembers() {
        TeamRequest request = new TeamRequest(
                "TEAM-GET",
                "Query Team",
                "Supports read scenarios",
                List.of(new MemberRequest("Liam", "Ray", "liam.get@example.com", "9091000001"))
        );

        AtomicLong teamId = new AtomicLong();
        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> teamId.set(((Number) id).longValue()));

        webTestClient.get()
                .uri("/api/v1/team/get/{teamId}", teamId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(teamId.get())
                .jsonPath("$.teamCode").isEqualTo("TEAM-GET")
                .jsonPath("$.members.length()").isEqualTo(1);
    }

    @Test
    void getAllTeams_shouldReturnAllPersistedTeams() {
        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(new TeamRequest(
                        "TEAM-ALL-1",
                        "All Team One",
                        "First team in list",
                        List.of(new MemberRequest("Ava", "Cole", "ava.all1@example.com", "9091000002"))
                ))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(new TeamRequest(
                        "TEAM-ALL-2",
                        "All Team Two",
                        "Second team in list",
                        List.of(new MemberRequest("Noah", "Shaw", "noah.all2@example.com", "9091000003"))
                ))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/team/get/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isNumber()
                .jsonPath("$[1].id").isNumber();

        assertThat(teamRepository.count()).isEqualTo(2L);
    }

    @Test
    void updateTeam_shouldReplaceMembersAndDeleteRemovedOnes() {
        AtomicLong teamId = new AtomicLong();
        AtomicLong oldMemberId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(new TeamRequest(
                        "TEAM-UPD",
                        "Updatable Team",
                        "Before update",
                        List.of(new MemberRequest("Ria", "Stone", "ria.before@example.com", "9091000004"))
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> teamId.set(((Number) id).longValue()))
                .jsonPath("$.members[0].id").value(id -> oldMemberId.set(((Number) id).longValue()));

        webTestClient.put()
                .uri("/api/v1/team/update/{teamId}", teamId.get())
                .bodyValue(new TeamRequest(
                        "TEAM-UPD",
                        "Updatable Team",
                        "After update",
                        List.of(new MemberRequest("Ria", "Stone", "ria.after@example.com", "9091000005"))
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(teamId.get())
                .jsonPath("$.description").isEqualTo("After update")
                .jsonPath("$.members[0].email").isEqualTo("ria.after@example.com");

        assertThat(memberRepository.existsById(oldMemberId.get())).isFalse();
    }

    @Test
    void updateTeam_withInvalidTeamCode_shouldReturnBadRequestAndKeepRecordUnchanged() {
        AtomicLong teamId = new AtomicLong();

        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(new TeamRequest(
                        "TEAM-IMM",
                        "Immutable Team",
                        "Original state",
                        List.of(new MemberRequest("Mia", "Lee", "mia.imm@example.com", "9091000006"))
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> teamId.set(((Number) id).longValue()));

        webTestClient.put()
                .uri("/api/v1/team/update/{teamId}", teamId.get())
                .bodyValue(new TeamRequest(
                        "",
                        "Mutated Team",
                        "Mutated state",
                        List.of(new MemberRequest("Mia", "Lee", "mia.changed@example.com", "9091000007"))
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("teamCode is required");

        webTestClient.get()
                .uri("/api/v1/team/get/{teamId}", teamId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.teamCode").isEqualTo("TEAM-IMM")
                .jsonPath("$.description").isEqualTo("Original state")
                .jsonPath("$.members[0].email").isEqualTo("mia.imm@example.com");
    }

    @Test
    void createTeam_withDuplicateTeamCode_shouldReturnConflict() {
        TeamRequest first = new TeamRequest(
                "TEAM-CONFLICT",
                "Conflict Team",
                "First create",
                List.of(new MemberRequest("Omar", "West", "omar.conflict1@example.com", "9091000008"))
        );

        TeamRequest duplicate = new TeamRequest(
                "TEAM-CONFLICT",
                "Conflict Team Duplicate",
                "Second create",
                List.of(new MemberRequest("Omar", "West", "omar.conflict2@example.com", "9091000009"))
        );

        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(first)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(duplicate)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Database constraint violation");
    }

    @Test
    void getTeam_withUnknownId_shouldReturnNotFound() {
        webTestClient.get()
                .uri("/api/v1/team/get/{teamId}", 999991L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Team not found with Id: 999991");
    }

    @Test
    void updateTeam_withUnknownId_shouldReturnNotFound() {
        webTestClient.put()
                .uri("/api/v1/team/update/{teamId}", 999992L)
                .bodyValue(new TeamRequest(
                        "TEAM-MISS",
                        "Missing Team",
                        "Missing update",
                        List.of(new MemberRequest("A", "B", "ab@example.com", "9091000010"))
                ))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Team not found with Id: 999992");
    }

    @Test
    void deleteTeam_withUnknownId_shouldReturnNotFoundAndKeepExistingDataUnchanged() {
        webTestClient.post()
                .uri("/api/v1/team/create")
                .bodyValue(new TeamRequest(
                        "TEAM-KEEP",
                        "Keep Team",
                        "Must remain after failed delete",
                        List.of(new MemberRequest("Kira", "Fox", "kira.keep@example.com", "9091000011"))
                ))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.delete()
                .uri("/api/v1/team/delete/{teamId}", 999993L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Team not found with Id: 999993");

        assertThat(teamRepository.count()).isEqualTo(1L);
        assertThat(memberRepository.count()).isEqualTo(1L);
    }
}

