package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "courses")
public class Course {

    private static final PersistenceUtil PERSISTENCE_UTIL = Persistence.getPersistenceUtil();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Setter
    @Column(nullable = false, length = 240)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "courses")
    private List<Student> students = new ArrayList<>();

    public void addStudent(Student student) {
        if (student == null) {
            return;
        }
        if (!students.contains(student)) {
            students.add(student);
        }
        if (PERSISTENCE_UTIL.isLoaded(student, "courses") && !student.getCourses().contains(this)) {
            student.getCourses().add(this);
        }
    }

    public void removeStudent(Student student) {
        if (student == null) {
            return;
        }
        students.remove(student);
        if (PERSISTENCE_UTIL.isLoaded(student, "courses")) {
            student.getCourses().remove(this);
        }
    }
}

