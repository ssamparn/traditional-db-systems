package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Course;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.StudentRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

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

    @BeforeEach
    void setupClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanup() {
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void createStudent_withCourses_shouldPersistAndReturnCreated() {
        Course courseOne = createCourse("DSA-101", "Data structures and algorithms");
        Course courseTwo = createCourse("DB-201", "Relational database systems");

        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Ava", "Peterson", "9093000001", "ava.student@example.com", List.of(courseOne.getId(), courseTwo.getId())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.email").isEqualTo("ava.student@example.com")
                .jsonPath("$.courses.length()").isEqualTo(2);
    }

    @Test
    void createStudent_withoutFirstName_shouldReturnBadRequest() {
        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(Map.of(
                        "lastName", "NoFirstName",
                        "mobile", "9093000002",
                        "email", "nofirst@example.com"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("firstName is required");
    }

    @Test
    void updateStudent_withUnknownId_shouldReturnNotFound() {
        webTestClient.put()
                .uri("/api/v1/student/update/{studentId}", 999991L)
                .bodyValue(new StudentRequest("Ghost", "User", "9093000003", "ghost@example.com", List.of()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Student not found with Id: 999991");
    }

    @Test
    void getStudent_withExistingId_shouldReturnStudentWithCourses() {
        Course course = createCourse("NET-110", "Networking basics");
        Long[] studentId = new Long[1];

        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Ethan", "Miles", "9093000005", "ethan.student@example.com", List.of(course.getId())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> studentId[0] = ((Number) id).longValue());

        webTestClient.get()
                .uri("/api/v1/student/get/{studentId}", studentId[0])
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(studentId[0])
                .jsonPath("$.email").isEqualTo("ethan.student@example.com")
                .jsonPath("$.courses.length()").isEqualTo(1);
    }

    @Test
    void getStudents_shouldReturnAllPersistedStudents() {
        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Mason", "Lopez", "9093000006", "mason.student@example.com", List.of()))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Riya", "Shah", "9093000007", "riya.student@example.com", List.of()))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/student/get/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[?(@.email == 'mason.student@example.com')]").exists()
                .jsonPath("$[?(@.email == 'riya.student@example.com')]").exists();
    }

    @Test
    void updateStudent_shouldReplaceCourseAssociations() {
        Course originalCourse = createCourse("SEC-220", "Security essentials");
        Course replacementCourse = createCourse("ARCH-420", "Architecture advanced");
        Long[] studentId = new Long[1];

        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Aiden", "Ross", "9093000008", "aiden.student@example.com", List.of(originalCourse.getId())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> studentId[0] = ((Number) id).longValue());

        webTestClient.put()
                .uri("/api/v1/student/update/{studentId}", studentId[0])
                .bodyValue(new StudentRequest("Aiden", "Ross", "9093000008", "aiden.student@example.com", List.of(replacementCourse.getId())))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(studentId[0])
                .jsonPath("$.courses.length()").isEqualTo(1)
                .jsonPath("$.courses[0].id").isEqualTo(replacementCourse.getId());
    }

    @Test
    void createStudent_withDuplicateEmail_shouldReturnConflict() {
        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Olivia", "Gale", "9093000009", "duplicate.student@example.com", List.of()))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Olivia", "Gale", "9093000012", "duplicate.student@example.com", List.of()))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Database constraint violation");
    }

    @Test
    void deleteStudent_shouldReturnDeletedStudent() {
        Course course = createCourse("OPS-320", "Operations management");
        Long[] studentId = new Long[1];

        webTestClient.post()
                .uri("/api/v1/student/create")
                .bodyValue(new StudentRequest("Mia", "Stone", "9093000004", "mia.student@example.com", List.of(course.getId())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> studentId[0] = ((Number) id).longValue());

        webTestClient.delete()
                .uri("/api/v1/student/delete/{studentId}", studentId[0])
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(studentId[0]);

        webTestClient.get()
                .uri("/api/v1/course/get/{courseId}", course.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(course.getId())
                .jsonPath("$.students.length()").isEqualTo(0);
    }

    private Course createCourse(String name, String description) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        return courseRepository.saveAndFlush(course);
    }
}


