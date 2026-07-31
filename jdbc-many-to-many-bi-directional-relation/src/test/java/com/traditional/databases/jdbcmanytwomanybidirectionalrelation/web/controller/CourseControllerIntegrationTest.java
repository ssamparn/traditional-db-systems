package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.CourseRequest;
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
class CourseControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

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
    void createCourse_withStudents_shouldPersistAndReturnCreated() {
        Student studentOne = createStudent("Ari", "West", "9093000010", "ari.course@example.com");
        Student studentTwo = createStudent("Noah", "Khan", "9093000011", "noah.course@example.com");

        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("JAVA-410", "Advanced Java engineering", List.of(studentOne.getId(), studentTwo.getId())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.name").isEqualTo("JAVA-410")
                .jsonPath("$.students.length()").isEqualTo(2);
    }

    @Test
    void createCourse_withoutName_shouldReturnBadRequest() {
        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(Map.of(
                        "description", "No course name"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("name is required");
    }

    @Test
    void getCourse_withUnknownId_shouldReturnNotFound() {
        webTestClient.get()
                .uri("/api/v1/course/get/{courseId}", 999992L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Course not found with Id: 999992");
    }

    @Test
    void getCourse_withExistingId_shouldReturnCourseWithStudents() {
        Student student = createStudent("Zara", "Page", "9093000012", "zara.course@example.com");
        Long[] courseId = new Long[1];

        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("CLOUD-300", "Cloud platform design", List.of(student.getId())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> courseId[0] = ((Number) id).longValue());

        webTestClient.get()
                .uri("/api/v1/course/get/{courseId}", courseId[0])
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(courseId[0])
                .jsonPath("$.name").isEqualTo("CLOUD-300")
                .jsonPath("$.students.length()").isEqualTo(1);
    }

    @Test
    void getCourses_shouldReturnAllPersistedCourses() {
        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("OPS-101", "Operations intro", List.of()))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("ML-205", "Machine learning essentials", List.of()))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/course/get/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[?(@.name == 'OPS-101')]").exists()
                .jsonPath("$[?(@.name == 'ML-205')]").exists();
    }

    @Test
    void updateCourse_shouldReplaceStudentAssociations() {
        Student originalStudent = createStudent("Luca", "Nash", "9093000013", "luca.course@example.com");
        Student replacementStudent = createStudent("Tia", "Quinn", "9093000014", "tia.course@example.com");
        Long[] courseId = new Long[1];

        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("API-500", "Distributed API design", List.of(originalStudent.getId())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> courseId[0] = ((Number) id).longValue());

        webTestClient.put()
                .uri("/api/v1/course/update/{courseId}", courseId[0])
                .bodyValue(new CourseRequest("API-500", "Distributed API design", List.of(replacementStudent.getId())))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(courseId[0])
                .jsonPath("$.students.length()").isEqualTo(1)
                .jsonPath("$.students[0].id").isEqualTo(replacementStudent.getId());
    }

    @Test
    void createCourse_withDuplicateName_shouldReturnConflict() {
        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("UNQ-777", "Unique name course", List.of()))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("UNQ-777", "Unique name course duplicate", List.of()))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Database constraint violation");
    }

    @Test
    void deleteCourse_shouldReturnDeletedCourse() {
        Student student = createStudent("Ari", "Dell", "9093000015", "ari.delete.course@example.com");
        Long[] courseId = new Long[1];

        webTestClient.post()
                .uri("/api/v1/course/create")
                .bodyValue(new CourseRequest("AUD-101", "Audit fundamentals", List.of(student.getId())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> courseId[0] = ((Number) id).longValue());

        webTestClient.delete()
                .uri("/api/v1/course/delete/{courseId}", courseId[0])
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(courseId[0]);

        webTestClient.get()
                .uri("/api/v1/student/get/{studentId}", student.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(student.getId())
                .jsonPath("$.courses.length()").isEqualTo(0);
    }

    private Student createStudent(String firstName, String lastName, String mobile, String email) {
        Student student = new Student();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setMobile(mobile);
        student.setEmail(email);
        return studentRepository.saveAndFlush(student);
    }
}


