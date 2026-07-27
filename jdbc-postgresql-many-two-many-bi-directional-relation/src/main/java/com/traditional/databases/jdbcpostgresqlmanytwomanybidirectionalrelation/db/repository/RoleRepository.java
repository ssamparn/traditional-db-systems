package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}

