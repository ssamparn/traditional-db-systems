package com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkstationRepository extends JpaRepository<Workstation, Long> {
}

