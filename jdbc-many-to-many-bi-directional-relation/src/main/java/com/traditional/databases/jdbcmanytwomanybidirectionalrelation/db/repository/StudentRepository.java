package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

	Optional<Student> findByEmail(String email);

	@Query("select distinct s from Student s left join fetch s.courses")
	List<Student> findAllWithCourses();

	@Query("select distinct s from Student s left join fetch s.courses where s.id = :studentId")
	Optional<Student> findByIdWithCourses(@Param("studentId") Long studentId);
}


