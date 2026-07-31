package com.paypulse.platform.jdbconetomanyunidirectionalrelation.entity.relations;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class UniDirectionalAssociationInMemoryTest {

    @Test
    void ownerSideReplacement_shouldBeDeterministicBeforePersistence() {
        Team team = createTeam("TEAM-INMEM", "In-memory Team");

        Member oldMember = createMember("Ava", "Miller", "ava.inmem@example.com", "9091000001");
        Member newMember = createMember("Noah", "Brown", "noah.inmem@example.com", "9091000002");

        team.addMember(oldMember);
        team.removeMember(oldMember);
        team.addMember(newMember);

        assertThat(team.getMembers()).containsExactly(newMember);
    }

    @Test
    void memberEntity_shouldNotContainTeamReference() {
        Field[] fields = Member.class.getDeclaredFields();

        boolean hasTeamReference = Arrays.stream(fields)
                .anyMatch(field -> field.getType().equals(Team.class));

        assertThat(hasTeamReference).isFalse();
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

