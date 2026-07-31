package com.traditional.databases.jdbcpostgresqlentityrelations.db.repository;

import com.traditional.databases.jdbcpostgresqlentityrelations.db.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
