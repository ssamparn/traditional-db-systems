package com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.db.repository;

import com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.db.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {

}
