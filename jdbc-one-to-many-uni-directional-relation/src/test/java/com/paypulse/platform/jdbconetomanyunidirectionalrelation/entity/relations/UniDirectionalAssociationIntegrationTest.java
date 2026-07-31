package com.paypulse.platform.jdbconetomanyunidirectionalrelation.entity.relations;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.MemberRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.TeamRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UniDirectionalAssociationIntegrationTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void cleanup() {
        memberRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void settingOwnerCollection_shouldPersistAssociationAfterPersist() {
        Team team = createTeam("TEAM-5001", "Core Team");
        team.addMember(createMember("Ana", "Moore", "ana.core@example.com", "9010000001"));

        Team saved = teamRepository.saveAndFlush(team);
        Team reloaded = teamRepository.findByIdWithMembers(saved.getId()).orElseThrow();

        assertThat(reloaded.getMembers()).hasSize(1);
        assertThat(reloaded.getMembers().getFirst().getEmail()).isEqualTo("ana.core@example.com");
    }

    @Test
    void replacingOwnerCollection_shouldReplaceAssociationBeforeFlushAndDeleteOldAssociationsAfterPersist() {
        Team team = createTeam("TEAM-5002", "Replacement Team");
        team.addMember(createMember("Ben", "Scott", "ben.old@example.com", "9010000002"));

        Team saved = teamRepository.saveAndFlush(team);
        Long oldMemberId = saved.getMembers().getFirst().getId();

        saved.removeMember(saved.getMembers().getFirst());
        saved.addMember(createMember("Ben", "Scott", "ben.new@example.com", "9010000003"));
        teamRepository.saveAndFlush(saved);

        Team reloaded = teamRepository.findByIdWithMembers(saved.getId()).orElseThrow();

        assertThat(reloaded.getMembers()).hasSize(1);
        assertThat(reloaded.getMembers().getFirst().getEmail()).isEqualTo("ben.new@example.com");
        assertThat(memberRepository.existsById(oldMemberId)).isFalse();
    }

    @Test
    void assigningIndependentMembersToTwoOwners_shouldPersistBothAssociationsAfterPersist() {
        Team firstTeam = createTeam("TEAM-5003", "First Team");
        Team secondTeam = createTeam("TEAM-5004", "Second Team");

        firstTeam.addMember(createMember("Cara", "Nash", "cara.first@example.com", "9010000004"));
        secondTeam.addMember(createMember("Drew", "Hill", "drew.second@example.com", "9010000005"));

        Team savedFirst = teamRepository.saveAndFlush(firstTeam);
        Team savedSecond = teamRepository.saveAndFlush(secondTeam);

        Team reloadedFirst = teamRepository.findByIdWithMembers(savedFirst.getId()).orElseThrow();
        Team reloadedSecond = teamRepository.findByIdWithMembers(savedSecond.getId()).orElseThrow();

        assertThat(reloadedFirst.getMembers()).hasSize(1);
        assertThat(reloadedSecond.getMembers()).hasSize(1);
        assertThat(reloadedFirst.getMembers().getFirst().getId()).isNotEqualTo(reloadedSecond.getMembers().getFirst().getId());
    }

    private Team createTeam(String code, String name) {
        Team team = new Team();
        team.setTeamCode(code);
        team.setName(name);
        team.setDescription(name + " description");
        return team;
    }

    private Member createMember(String firstName, String lastName, String email, String mobile) {
        Member member = new Member();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setMobile(mobile);
        return member;
    }
}

