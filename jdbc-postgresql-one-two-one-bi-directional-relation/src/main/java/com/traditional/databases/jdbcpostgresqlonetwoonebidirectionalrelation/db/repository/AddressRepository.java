package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.repository;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {

}
