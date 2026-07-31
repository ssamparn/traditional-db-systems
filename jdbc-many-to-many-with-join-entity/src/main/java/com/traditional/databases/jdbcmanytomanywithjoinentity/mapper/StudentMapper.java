package com.traditional.databases.jdbcmanytomanywithjoinentity.mapper;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Student;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.StudentRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.EnrollmentSummaryResponse;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.StudentResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentMapper {

    public Student toEntity(StudentRequest request) {
        Student student = new Student();
        applyRequest(student, request);
        return student;
    }

    public Student updateEntity(Student student, StudentRequest request) {
        applyRequest(student, request);
        return student;
    }

    private void applyRequest(Student student, StudentRequest request) {
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setMobile(request.getMobile());
        student.setEmail(request.getEmail());
    }

    public StudentResponse toResponse(Student student) {
        List<EnrollmentSummaryResponse> enrollments = student.getEnrollments().stream()
                .map(this::toEnrollmentSummary)
                .toList();

        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getMobile(),
                student.getEmail(),
                enrollments
        );
    }

    private EnrollmentSummaryResponse toEnrollmentSummary(Enrollment enrollment) {
        return new EnrollmentSummaryResponse(
                enrollment.getId(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus(),
                enrollment.getGrade(),
                enrollment.getCreatedBy(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getEmail(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getName()
        );
    }
}

