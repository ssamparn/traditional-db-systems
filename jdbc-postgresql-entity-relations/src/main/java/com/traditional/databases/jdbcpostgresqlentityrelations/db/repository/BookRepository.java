package com.traditional.databases.jdbcpostgresqlentityrelations.db.repository;

import com.traditional.databases.jdbcpostgresqlentityrelations.db.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
