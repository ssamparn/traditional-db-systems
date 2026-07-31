package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.entity.lifecycle;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Course;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service.CourseService;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service.StudentService;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.CourseRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.StudentRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.CourseSummaryResponse;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.CourseResponse;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.StudentResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ManyToManyAssociationLifecycleIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @AfterEach
    void cleanup() {
        studentRepository.deleteAll();
        courseRepository.deleteAll();
    }

    @Test
    void updateStudent_shouldReplaceCourseLinks() {
        Course oldCourse = courseRepository.saveAndFlush(createCourse("M2M-OLD", "Old course"));
        Course newCourse = courseRepository.saveAndFlush(createCourse("M2M-NEW", "New course"));

        StudentResponse created = studentService.createStudent(new StudentRequest(
                        "Iris",
                        "Cole",
                        "9095000001",
                        "iris.lifecycle@example.com",
                        List.of(oldCourse.getId())))
                .block();
        assertThat(created).isNotNull();

        Long studentId = created.getId();

        StudentResponse updated = studentService.updateStudent(studentId, new StudentRequest(
                        "Iris",
                        "Cole",
                        "9095000001",
                        "iris.lifecycle@example.com",
                        List.of(newCourse.getId())))
                .block();
        assertThat(updated).isNotNull();

        Course reloadedOld = courseRepository.findByIdWithStudents(oldCourse.getId()).orElseThrow();
        Course reloadedNew = courseRepository.findByIdWithStudents(newCourse.getId()).orElseThrow();
        List<CourseSummaryResponse> updatedCourses = updated == null ? List.of() : updated.getCourses();

        assertThat(updatedCourses).hasSize(1);
        assertThat(updatedCourses.getFirst().getId()).isEqualTo(newCourse.getId());
        assertThat(reloadedOld.getStudents()).isEmpty();
        assertThat(reloadedNew.getStudents()).hasSize(1);
    }

    @Test
    void deleteStudent_shouldRemoveJoinRowsAndKeepCourse() {
        Course course = courseRepository.saveAndFlush(createCourse("M2M-KEEP-COURSE", "Independent course"));

        StudentResponse created = studentService.createStudent(new StudentRequest(
                        "Lena",
                        "Ford",
                        "9095000002",
                        "lena.lifecycle@example.com",
                        List.of(course.getId())))
                .block();
        assertThat(created).isNotNull();

        Long studentId = created.getId();
        studentService.deleteStudent(studentId).block();

        Course reloadedCourse = courseRepository.findByIdWithStudents(course.getId()).orElseThrow();

        assertThat(studentRepository.findById(studentId)).isEmpty();
        assertThat(reloadedCourse.getStudents()).isEmpty();
    }

    @Test
    void deleteCourse_shouldRemoveJoinRowsAndKeepStudent() {
        Student student = studentRepository.saveAndFlush(createStudentForDeleteCourseJourney());

        CourseResponse created = courseService.createCourse(new CourseRequest(
                        "M2M-KEEP-STUDENT",
                        "Independent student",
                        List.of(student.getId())))
                .block();
        assertThat(created).isNotNull();

        Long courseId = created.getId();
        courseService.deleteCourse(courseId).block();

        Student reloadedStudent = studentRepository.findByIdWithCourses(student.getId()).orElseThrow();

        assertThat(courseRepository.findById(courseId)).isEmpty();
        assertThat(reloadedStudent.getCourses()).isEmpty();
    }

    private Student createStudentForDeleteCourseJourney() {
        Student student = new Student();
        student.setFirstName("Nora");
        student.setLastName("Hart");
        student.setMobile("9095000003");
        student.setEmail("nora.lifecycle@example.com");
        return student;
    }

    private Course createCourse(String name, String description) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        return course;
    }
}

