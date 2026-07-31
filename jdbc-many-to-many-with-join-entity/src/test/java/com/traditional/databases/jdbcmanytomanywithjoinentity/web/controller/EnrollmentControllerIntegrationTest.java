package com.traditional.databases.jdbcmanytomanywithjoinentity.web.controller;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.EnrollmentStatus;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Student;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.EnrollmentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.EnrollmentRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.EnrollmentUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.Instant;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EnrollmentControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

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
    void createEnrollment_shouldPersistBusinessFieldsAndReturnCreated() {
        Student student = studentRepository.saveAndFlush(createStudent("Iris", "Cole", "9096300001", "iris.enroll@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("SEC-220", "Security essentials"));

        webTestClient.post()
                .uri("/api/v1/enrollment/create")
                .bodyValue(new EnrollmentRequest(
                        student.getId(),
                        course.getId(),
                        Instant.parse("2026-02-01T00:00:00Z"),
                        EnrollmentStatus.ENROLLED,
                        new BigDecimal("87.50"),
                        "advisor-1"
                ))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.status").isEqualTo("ENROLLED")
                .jsonPath("$.grade").isEqualTo(87.50)
                .jsonPath("$.createdBy").isEqualTo("advisor-1")
                .jsonPath("$.student.id").isEqualTo(student.getId())
                .jsonPath("$.course.id").isEqualTo(course.getId());
    }

    @Test
    void createEnrollment_withDuplicateStudentCourse_shouldReturnBadRequest() {
        Student student = studentRepository.saveAndFlush(createStudent("Nora", "Hart", "9096300002", "nora.enroll@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("OPS-101", "Operations intro"));
        enrollmentRepository.saveAndFlush(createEnrollment(student, course, "seed-enrollment"));

        webTestClient.post()
                .uri("/api/v1/enrollment/create")
                .bodyValue(new EnrollmentRequest(
                        student.getId(),
                        course.getId(),
                        Instant.parse("2026-02-02T00:00:00Z"),
                        EnrollmentStatus.IN_PROGRESS,
                        new BigDecimal("75.00"),
                        "advisor-2"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Enrollment already exists for studentId=" + student.getId() + " and courseId=" + course.getId());
    }

    @Test
    void updateEnrollment_shouldChangeStatusAndGrade() {
        Student student = studentRepository.saveAndFlush(createStudent("Riya", "Shah", "9096300003", "riya.enroll@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("ML-205", "Machine learning essentials"));
        Enrollment enrollment = enrollmentRepository.saveAndFlush(createEnrollment(student, course, "seed-grade"));

        webTestClient.put()
                .uri("/api/v1/enrollment/update/{enrollmentId}", enrollment.getId())
                .bodyValue(new EnrollmentUpdateRequest(
                        Instant.parse("2026-03-10T10:00:00Z"),
                        EnrollmentStatus.COMPLETED,
                        new BigDecimal("91.25"),
                        "faculty-approver"
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(enrollment.getId())
                .jsonPath("$.status").isEqualTo("COMPLETED")
                .jsonPath("$.grade").isEqualTo(91.25)
                .jsonPath("$.createdBy").isEqualTo("faculty-approver");
    }

    @Test
    void getEnrollmentsByStudent_shouldReturnOnlyStudentEnrollments() {
        Student studentOne = studentRepository.saveAndFlush(createStudent("Aiden", "Ross", "9096300004", "aiden.enroll@example.com"));
        Student studentTwo = studentRepository.saveAndFlush(createStudent("Ethan", "Miles", "9096300005", "ethan.enroll@example.com"));
        Course courseOne = courseRepository.saveAndFlush(createCourse("CLOUD-300", "Cloud platform design"));
        Course courseTwo = courseRepository.saveAndFlush(createCourse("API-500", "Distributed API design"));

        enrollmentRepository.saveAndFlush(createEnrollment(studentOne, courseOne, "seed-filter-1"));
        enrollmentRepository.saveAndFlush(createEnrollment(studentOne, courseTwo, "seed-filter-2"));
        enrollmentRepository.saveAndFlush(createEnrollment(studentTwo, courseTwo, "seed-filter-3"));

        webTestClient.get()
                .uri("/api/v1/enrollment/get/by-student/{studentId}", studentOne.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].student.id").isEqualTo(studentOne.getId())
                .jsonPath("$[1].student.id").isEqualTo(studentOne.getId());
    }

    @Test
    void deleteEnrollment_shouldKeepStudentAndCourse() {
        Student student = studentRepository.saveAndFlush(createStudent("Zara", "Page", "9096300006", "zara.enroll@example.com"));
        Course course = courseRepository.saveAndFlush(createCourse("FIN-210", "Finance fundamentals"));
        Enrollment enrollment = enrollmentRepository.saveAndFlush(createEnrollment(student, course, "seed-delete"));

        webTestClient.delete()
                .uri("/api/v1/enrollment/delete/{enrollmentId}", enrollment.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(enrollment.getId());

        webTestClient.get()
                .uri("/api/v1/student/get/{studentId}", student.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(student.getId())
                .jsonPath("$.enrollments.length()").isEqualTo(0);

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
        enrollment.setEnrolledAt(Instant.parse("2026-01-01T00:00:00Z"));
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setGrade(new BigDecimal("79.00"));
        enrollment.setCreatedBy(createdBy);
        student.addEnrollment(enrollment);
        course.addEnrollment(enrollment);
        return enrollment;
    }
}

