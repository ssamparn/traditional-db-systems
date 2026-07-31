package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

