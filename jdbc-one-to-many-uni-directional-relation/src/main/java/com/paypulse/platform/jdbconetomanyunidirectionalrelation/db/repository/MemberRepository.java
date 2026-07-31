package com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "update members set team_id_fk = :teamId where id = :memberId", nativeQuery = true)
    int reassignMemberToTeam(@Param("memberId") Long memberId, @Param("teamId") Long teamId);
}

