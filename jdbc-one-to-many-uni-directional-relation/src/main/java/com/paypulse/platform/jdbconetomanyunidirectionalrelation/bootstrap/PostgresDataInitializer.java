package com.paypulse.platform.jdbconetomanyunidirectionalrelation.bootstrap;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.MemberRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Profile("postgres")
public class PostgresDataInitializer implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;

    public PostgresDataInitializer(TeamRepository teamRepository, MemberRepository memberRepository) {
        this.teamRepository = teamRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        if (teamRepository.count() > 0 || memberRepository.count() > 0) {
            log.info("Skipping postgres seed. Existing data found in teams/members tables.");
            return;
        }

        List<Team> teams = List.of(
                createTeamWithMembers(
                        "ENG-PLATFORM",
                        "Platform Engineering",
                        "Builds shared backend platform capabilities and standards.",
                        List.of(
                                createMember("Aarav", "Mehta", "aarav.mehta@paypulse.example.com", "+1-555-0101"),
                                createMember("Diya", "Shah", "diya.shah@paypulse.example.com", "+1-555-0102"),
                                createMember("Rohan", "Iyer", "rohan.iyer@paypulse.example.com", "+1-555-0103")
                        )
                ),
                createTeamWithMembers(
                        "ENG-PAYMENTS",
                        "Payments Engineering",
                        "Owns payment rails, settlement workflow, and transaction integrity.",
                        List.of(
                                createMember("Ishita", "Rao", "ishita.rao@paypulse.example.com", "+1-555-0201"),
                                createMember("Karan", "Verma", "karan.verma@paypulse.example.com", "+1-555-0202"),
                                createMember("Maya", "Nair", "maya.nair@paypulse.example.com", "+1-555-0203")
                        )
                ),
                createTeamWithMembers(
                        "ENG-RISK",
                        "Risk and Compliance Engineering",
                        "Builds fraud detection, risk scoring, and compliance controls.",
                        List.of(
                                createMember("Nikhil", "Kapoor", "nikhil.kapoor@paypulse.example.com", "+1-555-0301"),
                                createMember("Sana", "Khan", "sana.khan@paypulse.example.com", "+1-555-0302"),
                                createMember("Vikram", "Joshi", "vikram.joshi@paypulse.example.com", "+1-555-0303")
                        )
                )
        );

        teamRepository.saveAll(teams);

        int seededMemberCount = teams.stream().mapToInt(team -> team.getMembers().size()).sum();
        log.info("Postgres seed complete: inserted {} teams and {} members.", teams.size(), seededMemberCount);
    }

    private static Team createTeamWithMembers(String teamCode,
                                              String name,
                                              String description,
                                              List<Member> members) {
        Team team = new Team();
        team.setTeamCode(teamCode);
        team.setName(name);
        team.setDescription(description);
        members.forEach(team::addMember);
        return team;
    }

    private static Member createMember(String firstName, String lastName, String email, String mobile) {
        Member member = new Member();
        member.setFirstName(firstName);
        member.setLastName(lastName);
        member.setEmail(email);
        member.setMobile(mobile);
        return member;
    }
}
