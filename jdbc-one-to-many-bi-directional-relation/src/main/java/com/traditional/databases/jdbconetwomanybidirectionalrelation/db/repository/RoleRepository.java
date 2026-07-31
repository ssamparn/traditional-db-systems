package com.traditional.databases.jdbconetwomanybidirectionalrelation.db.repository;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.db.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query("select distinct r from roles r left join fetch r.users")
    List<Role> findAllWithUsers();

    @Query("select distinct r from roles r left join fetch r.users where r.id = :roleId")
    Optional<Role> findByIdWithUsers(@Param("roleId") Long roleId);
}