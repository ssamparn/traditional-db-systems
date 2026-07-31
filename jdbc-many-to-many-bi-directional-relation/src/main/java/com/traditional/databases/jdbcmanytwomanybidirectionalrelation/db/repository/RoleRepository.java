package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}

