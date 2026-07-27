package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.repository;

import com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

