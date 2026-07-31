package com.traditional.databases.jdbcmanytomanywithjoinentity.web.controller;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.EnrollmentStatus;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Student;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.EnrollmentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.CourseRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CourseControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void setupClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanup() {
        enrollmentRepository.deleteAll();
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void createCourse_shouldPersistAndReturnCreated() {
        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("JAVA-410", "Advanced Java engineering"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.name").isEqualTo("JAVA-410")
                .jsonPath("$.enrollments.length()").isEqualTo(0);
    }

    @Test
    void getCourse_shouldReturnEnrollmentsWithBusinessFields() {
        Student student = studentRepository.saveAndFlush(createStudent("Ari", "West", "9096200001", "ari.join@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("NET-110", "Networking fundamentals"));
        enrollmentRepository.saveAndFlush(createEnrollment(student, course, "seed-course-user"));

        webTestClient.get()
                .uri("/api/v1/course/get/{courseId}", course.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(course.getId())
                .jsonPath("$.enrollments.length()").isEqualTo(1)
                .jsonPath("$.enrollments[0].studentEmail").isEqualTo("ari.join@example.com")
                .jsonPath("$.enrollments[0].status").isEqualTo("ENROLLED")
                .jsonPath("$.enrollments[0].createdBy").isEqualTo("seed-course-user");
    }

    @Test
    void createCourse_withDuplicateName_shouldReturnConflict() {
        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("UNQ-777", "Unique name course"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("UNQ-777", "Duplicate unique name course"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Database constraint violation");
    }

    @Test
    void deleteCourse_shouldDeleteEnrollmentsAndKeepStudent() {
        Student student = studentRepository.saveAndFlush(createStudent("Tia", "Quinn", "9096200002", "tia.join@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("AUD-101", "Audit fundamentals"));
        enrollmentRepository.saveAndFlush(createEnrollment(student, course, "ops-course"));

        webTestClient.delete()
                .uri("/api/v1/course/delete/{courseId}", course.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(course.getId());

        webTestClient.get()
                .uri("/api/v1/student/get/{studentId}", student.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(student.getId())
                .jsonPath("$.enrollments.length()").isEqualTo(0);
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
        enrollment.setEnrolledAt(Instant.parse("2026-01-06T07:30:00Z"));
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setCreatedBy(createdBy);
        student.addEnrollment(enrollment);
        course.addEnrollment(enrollment);
        return enrollment;
    }
}

