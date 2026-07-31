package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.controller;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.MemberRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.TeamRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MemberControllerIntegrationTest {

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
    void createMember_underExistingTeam_shouldReturnCreated() {
        Team team = createTeam("TEAM-QA", "Quality Team");

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Ria", "Shah", "ria.qa@example.com", "9090000101"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.teamId").isEqualTo(team.getId())
                .jsonPath("$.email").isEqualTo("ria.qa@example.com");
    }

    @Test
    void reassignMemberTeam_shouldMoveMemberToTargetTeam() {
        Team sourceTeam = createTeam("TEAM-SRC", "Source Team");
        Team targetTeam = createTeam("TEAM-TGT", "Target Team");

        Member member = new Member();
        member.setFirstName("Eli");
        member.setLastName("Ford");
        member.setEmail("eli.team@example.com");
        member.setMobile("9090000102");

        sourceTeam.addMember(member);
        sourceTeam = teamRepository.saveAndFlush(sourceTeam);
        Long memberId = sourceTeam.getMembers().getFirst().getId();

        webTestClient.put()
                .uri("/api/v1/member/reassign/{memberId}/team/{teamId}", memberId, targetTeam.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(memberId)
                .jsonPath("$.teamId").isEqualTo(targetTeam.getId());

        Team reloadedSource = teamRepository.findByIdWithMembers(sourceTeam.getId()).orElseThrow();
        Team reloadedTarget = teamRepository.findByIdWithMembers(targetTeam.getId()).orElseThrow();

        assertThat(reloadedSource.getMembers()).extracting(Member::getId).doesNotContain(memberId);
        assertThat(reloadedTarget.getMembers()).extracting(Member::getId).contains(memberId);
    }

    @Test
    void updateMember_withInvalidEmail_shouldReturnBadRequestAndNotMutateRecord() {
        Team team = new Team();
        team.setTeamCode("TEAM-APP");
        team.setName("Application Team");
        team.setDescription("Application delivery squad");

        Member member = new Member();
        member.setFirstName("Luca");
        member.setLastName("Hall");
        member.setEmail("luca.app@example.com");
        member.setMobile("9090000103");
        team.addMember(member);

        Team saved = teamRepository.saveAndFlush(team);
        Long memberId = saved.getMembers().getFirst().getId();

        webTestClient.put()
                .uri("/api/v1/member/update/{memberId}", memberId)
                .bodyValue(new MemberRequest("Luca", "Hall", "invalid-email", "9090000104"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("member.email must be a valid email address");

        Member unchanged = memberRepository.findById(memberId).orElseThrow();
        assertThat(unchanged.getEmail()).isEqualTo("luca.app@example.com");
        assertThat(unchanged.getMobile()).isEqualTo("9090000103");
    }

    private Team createTeam(String code, String name) {
        Team team = new Team();
        team.setTeamCode(code);
        team.setName(name);
        team.setDescription(name + " description");
        return teamRepository.saveAndFlush(team);
    }
}

