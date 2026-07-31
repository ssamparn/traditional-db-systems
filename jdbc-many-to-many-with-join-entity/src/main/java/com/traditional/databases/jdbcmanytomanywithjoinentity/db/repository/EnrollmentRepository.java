package com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Query("select e from Enrollment e join fetch e.student join fetch e.course")
    List<Enrollment> findAllWithStudentAndCourse();

    @Query("select e from Enrollment e join fetch e.student join fetch e.course where e.id = :enrollmentId")
    Optional<Enrollment> findByIdWithStudentAndCourse(@Param("enrollmentId") Long enrollmentId);

    @Query("select e from Enrollment e join fetch e.student join fetch e.course where e.student.id = :studentId")
    List<Enrollment> findAllByStudentIdWithStudentAndCourse(@Param("studentId") Long studentId);

    @Query("select e from Enrollment e join fetch e.student join fetch e.course where e.course.id = :courseId")
    List<Enrollment> findAllByCourseIdWithStudentAndCourse(@Param("courseId") Long courseId);

    @Query("select e from Enrollment e where e.student.id = :studentId and e.course.id = :courseId")
    Optional<Enrollment> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}

