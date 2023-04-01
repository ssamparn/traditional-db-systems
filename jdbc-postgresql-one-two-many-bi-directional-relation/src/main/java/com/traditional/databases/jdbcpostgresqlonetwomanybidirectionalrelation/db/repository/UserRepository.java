package com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.db.repository;

import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.db.entity.User;
import com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response.RoleUserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT new com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response.RoleUserResponse(r.id, r.name, u.id, u.firstName, u.lastName, u.email) from roles r, users u join r.users")
    List<RoleUserResponse> getJoinInformation();
}