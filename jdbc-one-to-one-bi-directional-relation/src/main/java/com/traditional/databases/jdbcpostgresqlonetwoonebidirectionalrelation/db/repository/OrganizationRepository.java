package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.repository;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}
