package com.paypulse.platform.jdbconetomanyunidirectionalrelation.entity.orphanhandling;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.MemberRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.TeamRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.service.TeamService;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.TeamRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TeamOrphanHandlingIntegrationTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamService teamService;

    @AfterEach
    void cleanup() {
        memberRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void updateTeam_shouldRemoveMembersDeletedFromCollectionAsOrphans() {
        Team team = new Team();
        team.setTeamCode("TEAM-ORPH-1");
        team.setName("Observability Team");
        team.setDescription("Owns observability stack");

        Member first = new Member();
        first.setFirstName("Liam");
        first.setLastName("Gray");
        first.setEmail("liam.orph@example.com");
        first.setMobile("9020000001");

        Member second = new Member();
        second.setFirstName("Nia");
        second.setLastName("Ray");
        second.setEmail("nia.orph@example.com");
        second.setMobile("9020000002");

        team.addMember(first);
        team.addMember(second);

        Team saved = teamRepository.saveAndFlush(team);
        Long removedMemberId = saved.getMembers().getFirst().getId();

        TeamRequest updateRequest = new TeamRequest(
                "TEAM-ORPH-1",
                "Observability Team",
                "Updated observability ownership",
                List.of(new MemberRequest("Omar", "Lee", "omar.orph@example.com", "9020000003"))
        );

        teamService.updateTeam(saved.getId(), updateRequest).block();

        Team updated = teamRepository.findByIdWithMembers(saved.getId()).orElseThrow();

        assertThat(updated.getMembers()).hasSize(1);
        assertThat(updated.getMembers().getFirst().getEmail()).isEqualTo("omar.orph@example.com");
        assertThat(memberRepository.existsById(removedMemberId)).isFalse();
    }

    @Test
    void deleteTeam_shouldCascadeDeleteMembers() {
        Team team = new Team();
        team.setTeamCode("TEAM-ORPH-2");
        team.setName("Compliance Team");
        team.setDescription("Owns compliance checks");

        Member member = new Member();
        member.setFirstName("Sara");
        member.setLastName("Jain");
        member.setEmail("sara.orph@example.com");
        member.setMobile("9020000004");

        team.addMember(member);

        Team saved = teamRepository.saveAndFlush(team);
        Long memberId = saved.getMembers().getFirst().getId();

        teamService.deleteTeam(saved.getId()).block();

        assertThat(teamRepository.existsById(saved.getId())).isFalse();
        assertThat(memberRepository.existsById(memberId)).isFalse();
    }
}

