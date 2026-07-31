package com.traditional.databases.jdbcmanytomanywithjoinentity.entity.lifecycle;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.EnrollmentStatus;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Student;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.EnrollmentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EnrollmentLifecycleIntegrationTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @AfterEach
    void cleanup() {
        enrollmentRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void deletingStudent_shouldCascadeDeleteEnrollmentAndKeepCourse() {
        Student student = studentRepository.saveAndFlush(createStudent("Liam", "Ray", "9096400001", "liam.lifecycle@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("PLATFORM", "Platform course"));
        Enrollment enrollment = enrollmentRepository.saveAndFlush(createEnrollment(student, course, "lifecycle-seed"));

        studentRepository.delete(student);
        studentRepository.flush();

        assertThat(studentRepository.findById(student.getId())).isEmpty();
        assertThat(enrollmentRepository.findById(enrollment.getId())).isEmpty();
        assertThat(courseRepository.findByIdWithEnrollments(course.getId()).orElseThrow().getEnrollments()).isEmpty();
    }

    @Test
    void deletingCourse_shouldCascadeDeleteEnrollmentAndKeepStudent() {
        Student student = studentRepository.saveAndFlush(createStudent("Mia", "Cole", "9096400002", "mia.lifecycle@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("SECURITY", "Security course"));
        Enrollment enrollment = enrollmentRepository.saveAndFlush(createEnrollment(student, course, "lifecycle-seed-2"));

        courseRepository.delete(course);
        courseRepository.flush();

        assertThat(courseRepository.findById(course.getId())).isEmpty();
        assertThat(enrollmentRepository.findById(enrollment.getId())).isEmpty();
        assertThat(studentRepository.findByIdWithEnrollments(student.getId()).orElseThrow().getEnrollments()).isEmpty();
    }

    private Student createStudent(String firstName, String lastName, String mobile, String email) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setMobile(mobile);
        student.setEmail(email);
        return student;
    }

    private Course createCourse(String name, String description) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        return course;
    }

    private Enrollment createEnrollment(Student student, Course course, String createdBy) {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt(Instant.parse("2026-03-01T00:00:00Z"));
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setCreatedBy(createdBy);
        student.addEnrollment(enrollment);
        course.addEnrollment(enrollment);
        return enrollment;
    }
}

