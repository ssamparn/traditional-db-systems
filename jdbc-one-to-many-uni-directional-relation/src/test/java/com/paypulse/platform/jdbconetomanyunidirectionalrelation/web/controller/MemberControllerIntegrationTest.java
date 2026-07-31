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

    @Test
    void getMemberById_shouldReturnPersistedMember() {
        Team team = createTeam("TEAM-GET-MEMBER", "Getter Team");

        Long[] memberIdHolder = new Long[1];
        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Nora", "Hill", "nora.get@example.com", "9092000001"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> memberIdHolder[0] = ((Number) id).longValue());

        webTestClient.get()
                .uri("/api/v1/member/get/{memberId}", memberIdHolder[0])
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(memberIdHolder[0])
                .jsonPath("$.email").isEqualTo("nora.get@example.com")
                .jsonPath("$.teamId").isEqualTo(team.getId());
    }

    @Test
    void getAllMembers_shouldReturnAllPersistedMembers() {
        Team firstTeam = createTeam("TEAM-LIST-1", "First List Team");
        Team secondTeam = createTeam("TEAM-LIST-2", "Second List Team");

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", firstTeam.getId())
                .bodyValue(new MemberRequest("Ava", "Mills", "ava.list@example.com", "9092000002"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", secondTeam.getId())
                .bodyValue(new MemberRequest("Eli", "Shaw", "eli.list@example.com", "9092000003"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/member/get/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isNumber()
                .jsonPath("$[1].id").isNumber();

        assertThat(memberRepository.count()).isEqualTo(2L);
    }

    @Test
    void updateMember_shouldPersistProfileChangesAndKeepOwnership() {
        Team team = createTeam("TEAM-UPD-MBR", "Update Member Team");

        Long[] memberIdHolder = new Long[1];
        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Mia", "Stone", "mia.before@example.com", "9092000004"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> memberIdHolder[0] = ((Number) id).longValue());

        webTestClient.put()
                .uri("/api/v1/member/update/{memberId}", memberIdHolder[0])
                .bodyValue(new MemberRequest("Mia", "Stone", "mia.after@example.com", "9092009999"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(memberIdHolder[0])
                .jsonPath("$.email").isEqualTo("mia.after@example.com")
                .jsonPath("$.mobile").isEqualTo("9092009999")
                .jsonPath("$.teamId").isEqualTo(team.getId());

        Member updated = memberRepository.findById(memberIdHolder[0]).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("mia.after@example.com");
    }

    @Test
    void deleteMember_shouldRemoveMemberAndKeepTeam() {
        Team team = createTeam("TEAM-DEL-MBR", "Delete Member Team");

        Long[] memberIdHolder = new Long[1];
        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Luca", "Dane", "luca.delete@example.com", "9092000005"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> memberIdHolder[0] = ((Number) id).longValue());

        webTestClient.delete()
                .uri("/api/v1/member/delete/{memberId}", memberIdHolder[0])
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(memberIdHolder[0]);

        assertThat(memberRepository.existsById(memberIdHolder[0])).isFalse();
        assertThat(teamRepository.existsById(team.getId())).isTrue();
    }

    @Test
    void createMember_withUnknownTeam_shouldReturnNotFound() {
        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", 999994L)
                .bodyValue(new MemberRequest("Ghost", "User", "ghost@example.com", "9092000006"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Team not found with Id: 999994");
    }

    @Test
    void createMember_withDuplicateEmail_shouldReturnConflict() {
        Team firstTeam = createTeam("TEAM-CONF-1", "Conflict Team One");
        Team secondTeam = createTeam("TEAM-CONF-2", "Conflict Team Two");

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", firstTeam.getId())
                .bodyValue(new MemberRequest("Ari", "Lane", "ari.conflict@example.com", "9092000007"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", secondTeam.getId())
                .bodyValue(new MemberRequest("Ari", "Lane", "ari.conflict@example.com", "9092000008"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Database constraint violation");
    }

    @Test
    void getMember_withUnknownId_shouldReturnNotFound() {
        webTestClient.get()
                .uri("/api/v1/member/get/{memberId}", 999995L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Member not found with Id: 999995");
    }

    @Test
    void updateMember_withUnknownId_shouldReturnNotFound() {
        webTestClient.put()
                .uri("/api/v1/member/update/{memberId}", 999996L)
                .bodyValue(new MemberRequest("X", "Y", "xy@example.com", "9092000009"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Member not found with Id: 999996");
    }

    @Test
    void deleteMember_withUnknownId_shouldReturnNotFound() {
        webTestClient.delete()
                .uri("/api/v1/member/delete/{memberId}", 999997L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Member not found with Id: 999997");
    }

    @Test
    void reassignMember_withUnknownTeam_shouldReturnNotFound() {
        Team sourceTeam = createTeam("TEAM-RS-UNK", "Reassign Source");
        Long[] memberIdHolder = new Long[1];

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", sourceTeam.getId())
                .bodyValue(new MemberRequest("Rin", "Vale", "rin.reassign@example.com", "9092000010"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> memberIdHolder[0] = ((Number) id).longValue());

        webTestClient.put()
                .uri("/api/v1/member/reassign/{memberId}/team/{teamId}", memberIdHolder[0], 999998L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Team not found with Id: 999998");
    }

    @Test
    void reassignMember_withUnknownMember_shouldReturnNotFound() {
        Team targetTeam = createTeam("TEAM-RM-UNK", "Reassign Target");

        webTestClient.put()
                .uri("/api/v1/member/reassign/{memberId}/team/{teamId}", 999999L, targetTeam.getId())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Member not found with Id: 999999");
    }

    @Test
    void getTeamMemberInfo_shouldReturnJoinedProjection() {
        Team team = createTeam("TEAM-JOIN", "Join Projection Team");

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Ari", "North", "ari.join@example.com", "9092000011"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/team/member/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].teamId").isNumber()
                .jsonPath("$[0].memberId").isNumber();
    }

    @Test
    void createMember_withInvalidEmail_shouldReturnBadRequest() {
        Team team = createTeam("TEAM-INV-EMAIL", "Invalid Email Team");

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Ari", "Vale", "invalid-email", "9092000012"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("member.email must be a valid email address");
    }

    @Test
    void createMember_withInvalidMobileLength_shouldReturnBadRequest() {
        Team team = createTeam("TEAM-INV-MOB", "Invalid Mobile Team");

        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Ari", "Vale", "ari.mobile@example.com", "123456789012345678901"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("member.mobile must be at most 20 characters");
    }

    @Test
    void updateMember_withInvalidMobileLength_shouldReturnBadRequestAndKeepRecordUnchanged() {
        Team team = createTeam("TEAM-UPD-INV", "Update Invalid Team");

        Long[] memberIdHolder = new Long[1];
        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Mia", "Stone", "mia.mobile.before@example.com", "9092000013"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> memberIdHolder[0] = ((Number) id).longValue());

        webTestClient.put()
                .uri("/api/v1/member/update/{memberId}", memberIdHolder[0])
                .bodyValue(new MemberRequest("Mia", "Stone", "mia.mobile.after@example.com", "123456789012345678901"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("member.mobile must be at most 20 characters");

        Member unchanged = memberRepository.findById(memberIdHolder[0]).orElseThrow();
        assertThat(unchanged.getEmail()).isEqualTo("mia.mobile.before@example.com");
        assertThat(unchanged.getMobile()).isEqualTo("9092000013");
    }

    @Test
    void reassignMember_toSameTeam_shouldRemainStableAndReturnOk() {
        Team team = createTeam("TEAM-SAME", "Same Team");

        Long[] memberIdHolder = new Long[1];
        webTestClient.post()
                .uri("/api/v1/member/create/team/{teamId}", team.getId())
                .bodyValue(new MemberRequest("Ria", "Khan", "ria.same@example.com", "9092000014"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> memberIdHolder[0] = ((Number) id).longValue());

        webTestClient.put()
                .uri("/api/v1/member/reassign/{memberId}/team/{teamId}", memberIdHolder[0], team.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(memberIdHolder[0])
                .jsonPath("$.teamId").isEqualTo(team.getId());

        Team reloadedTeam = teamRepository.findByIdWithMembers(team.getId()).orElseThrow();
        assertThat(reloadedTeam.getMembers()).extracting(Member::getId).contains(memberIdHolder[0]);
    }

    @Test
    void getTeamMemberInfo_whenNoData_shouldReturnEmptyArray() {
        webTestClient.get()
                .uri("/api/v1/team/member/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    private Team createTeam(String code, String name) {
        Team team = new Team();
        team.setTeamCode(code);
        team.setName(name);
        team.setDescription(name + " description");
        return teamRepository.saveAndFlush(team);
    }
}

