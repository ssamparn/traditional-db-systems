package com.traditional.databases.jdbcpostgresqlentityrelations.db.repository;

import com.traditional.databases.jdbcpostgresqlentityrelations.db.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
