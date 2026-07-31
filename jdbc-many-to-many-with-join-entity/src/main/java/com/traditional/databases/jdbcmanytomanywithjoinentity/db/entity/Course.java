package com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Setter
    @Column(nullable = false, length = 240)
    private String description;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    public void addEnrollment(Enrollment enrollment) {
        if (enrollment == null || enrollments.contains(enrollment)) {
            return;
        }
        enrollments.add(enrollment);
        enrollment.assignCourse(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            return;
        }
        if (enrollments.remove(enrollment)) {
            enrollment.assignCourse(null);
        }
    }
}

