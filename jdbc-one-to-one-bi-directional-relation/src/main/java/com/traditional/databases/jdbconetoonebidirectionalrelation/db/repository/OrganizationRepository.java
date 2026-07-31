package com.traditional.databases.jdbconetoonebidirectionalrelation.db.repository;

import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}
