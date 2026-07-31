package com.traditional.databases.jdbcpostgresqlentityrelations.db.repository;

import com.traditional.databases.jdbcpostgresqlentityrelations.db.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

}
