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
}

