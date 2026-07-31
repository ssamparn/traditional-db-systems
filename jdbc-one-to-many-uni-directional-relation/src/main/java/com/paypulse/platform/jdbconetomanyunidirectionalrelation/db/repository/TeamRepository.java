package com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.TeamMemberResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByTeamCode(String teamCode);

    @Query("select distinct t from teams t left join fetch t.members")
    List<Team> findAllWithMembers();

    @Query("select distinct t from teams t left join fetch t.members where t.id = :teamId")
    Optional<Team> findByIdWithMembers(@Param("teamId") Long teamId);

    @Query("select new com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.TeamMemberResponse(" +
            "t.id, t.teamCode, m.id, m.firstName, m.lastName, m.email) " +
            "from teams t join t.members m")
    List<TeamMemberResponse> getJoinInformation();

    @Query("select t.id from teams t join t.members m where m.id = :memberId")
    Optional<Long> findTeamIdByMemberId(@Param("memberId") Long memberId);
}

