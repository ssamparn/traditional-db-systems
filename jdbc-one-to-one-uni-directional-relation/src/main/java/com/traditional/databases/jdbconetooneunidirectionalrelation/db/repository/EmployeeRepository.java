package com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
}

