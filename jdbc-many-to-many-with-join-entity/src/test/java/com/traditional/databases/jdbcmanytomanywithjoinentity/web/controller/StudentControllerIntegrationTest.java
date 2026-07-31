package com.traditional.databases.jdbcmanytomanywithjoinentity.web.controller;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.EnrollmentStatus;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Student;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.EnrollmentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.StudentRequest;
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
class StudentControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

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
    void createStudent_shouldPersistAndReturnCreated() {
        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Ava", "Patel", "9096100001", "ava.join@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.email").isEqualTo("ava.join@example.com")
                .jsonPath("$.enrollments.length()").isEqualTo(0);
    }

    @Test
    void getStudent_shouldReturnEnrollmentsWithBusinessFields() {
        Student student = studentRepository.saveAndFlush(createStudent("Mia", "Stone", "9096100002", "mia.join@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("ARCH-201", "Architecture basics"));
        enrollmentRepository.saveAndFlush(createEnrollment(student, course, "seed-user"));

        webTestClient.get()
                .uri("/api/v1/student/get/{studentId}", student.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(student.getId())
                .jsonPath("$.enrollments.length()").isEqualTo(1)
                .jsonPath("$.enrollments[0].courseName").isEqualTo("ARCH-201")
                .jsonPath("$.enrollments[0].status").isEqualTo("ENROLLED")
                .jsonPath("$.enrollments[0].createdBy").isEqualTo("seed-user");
    }

    @Test
    void createStudent_withDuplicateEmail_shouldReturnConflict() {
        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Noah", "Lee", "9096100003", "duplicate.student.join@example.com"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Noah", "Lee", "9096100004", "duplicate.student.join@example.com"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Database constraint violation");
    }

    @Test
    void deleteStudent_shouldDeleteEnrollmentsAndKeepCourse() {
        Student student = studentRepository.saveAndFlush(createStudent("Lena", "Ford", "9096100005", "lena.join@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("DB-300", "Databases advanced"));
        enrollmentRepository.saveAndFlush(createEnrollment(student, course, "ops-user"));

        webTestClient.delete()
                .uri("/api/v1/student/delete/{studentId}", student.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(student.getId());

        webTestClient.get()
                .uri("/api/v1/course/get/{courseId}", course.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(course.getId())
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
        enrollment.setEnrolledAt(Instant.parse("2026-01-05T10:15:30Z"));
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setCreatedBy(createdBy);
        student.addEnrollment(enrollment);
        course.addEnrollment(enrollment);
        return enrollment;
    }
}

