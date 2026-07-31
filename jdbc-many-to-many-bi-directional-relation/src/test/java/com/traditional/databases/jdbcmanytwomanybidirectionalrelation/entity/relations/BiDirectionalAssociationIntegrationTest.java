package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.entity.relations;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Course;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BiDirectionalAssociationIntegrationTest {

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
    void addingCoursesFromOwningSide_shouldPersistJoinRowsAfterPersist() {
        Course courseOne = courseRepository.saveAndFlush(createCourse("PLATFORM", "Platform course"));
        Course courseTwo = courseRepository.saveAndFlush(createCourse("SECURITY", "Security course"));

        Student student = createStudent("Liam", "Ray", "9094000010", "liam.join@example.com");
        student.addCourse(courseOne);
        student.addCourse(courseTwo);

        Student saved = studentRepository.saveAndFlush(student);
        Student reloaded = studentRepository.findByIdWithCourses(saved.getId()).orElseThrow();

        assertThat(reloaded.getCourses()).hasSize(2);
    }

    @Test
    void replacingOwningSideCourses_shouldReplaceJoinRowsBeforeFlushAndPersistAfterPersist() {
        Course oldCourse = courseRepository.saveAndFlush(createCourse("OLD_COURSE", "Old course"));
        Course newCourse = courseRepository.saveAndFlush(createCourse("NEW_COURSE", "New course"));

        Student student = createStudent("Mia", "Stone", "9094000011", "mia.replace@example.com");
        student.addCourse(oldCourse);
        Student saved = studentRepository.saveAndFlush(student);

        saved.removeCourse(oldCourse);
        saved.addCourse(newCourse);
        studentRepository.saveAndFlush(saved);

        Student reloaded = studentRepository.findByIdWithCourses(saved.getId()).orElseThrow();

        assertThat(reloaded.getCourses()).hasSize(1);
        assertThat(reloaded.getCourses().getFirst().getName()).isEqualTo("NEW_COURSE");
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
}

