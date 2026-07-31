package com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByName(String name);

    @Query("select distinct c from Course c left join fetch c.enrollments e left join fetch e.student")
    List<Course> findAllWithEnrollments();

    @Query("select distinct c from Course c left join fetch c.enrollments e left join fetch e.student where c.id = :courseId")
    Optional<Course> findByIdWithEnrollments(@Param("courseId") Long courseId);
}

