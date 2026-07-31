package com.traditional.databases.jdbcmanytomanywithjoinentity.mapper;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Student;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.CourseSummaryResponse;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.EnrollmentResponse;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.StudentSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getEnrolledAt(),
                enrollment.getStatus(),
                enrollment.getGrade(),
                enrollment.getCreatedBy(),
                student == null ? null : new StudentSummaryResponse(
                        student.getId(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getEmail()
                ),
                course == null ? null : new CourseSummaryResponse(
                        course.getId(),
                        course.getName(),
                        course.getDescription()
                )
        );
    }
}

