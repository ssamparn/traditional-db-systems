package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "students")
public class Student {

    private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Setter
    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Setter
    @Column(nullable = false, length = 20)
    private String mobile;

    @Setter
    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "students_courses",
        joinColumns = @JoinColumn(name = "student_id_fk"),
        inverseJoinColumns = @JoinColumn(name = "course_id_fk")
    )
    private List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        if (course == null) {
            return;
        }
        if (!courses.contains(course)) {
            courses.add(course);
        }
        if (PERSISTENCE_UTIL.isLoaded(course, "students") && !course.getStudents().contains(this)) {
            course.getStudents().add(this);
        }
    }

    public void removeCourse(Course course) {
        if (course == null) {
            return;
        }
        courses.remove(course);
        if (PERSISTENCE_UTIL.isLoaded(course, "students")) {
            course.getStudents().remove(this);
        }
    }
}

