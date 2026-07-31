package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.entity.relations;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Course;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BiDirectionalAssociationInMemoryTest {

    @Test
    void addingFromOwningSide_shouldSynchronizeInverseBeforePersistence() {
        Student student = createStudent("Ava", "Cole", "9094000001", "ava.memory@example.com");
        Course course = createCourse("OPS", "Operations role");

        student.addCourse(course);

        assertThat(student.getCourses()).contains(course);
        assertThat(course.getStudents()).contains(student);
    }

    @Test
    void removingFromInverseSide_shouldDetachOwningSideBeforePersistence() {
        Student student = createStudent("Noah", "Shaw", "9094000002", "noah.memory@example.com");
        Course course = createCourse("AUDIT", "Audit role");

        course.addStudent(student);
        course.removeStudent(student);

        assertThat(course.getStudents()).doesNotContain(student);
        assertThat(student.getCourses()).doesNotContain(course);
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

