package com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.db.repository;

import com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.db.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}
