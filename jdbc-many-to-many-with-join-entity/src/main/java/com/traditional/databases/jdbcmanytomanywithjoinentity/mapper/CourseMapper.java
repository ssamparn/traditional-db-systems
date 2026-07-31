package com.traditional.databases.jdbcmanytomanywithjoinentity.mapper;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.CourseRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.CourseResponse;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.EnrollmentSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequest request) {
        Course course = new Course();
        applyRequest(course, request);
        return course;
    }

    public Course updateEntity(Course course, CourseRequest request) {
        applyRequest(course, request);
        return course;
    }

    private void applyRequest(Course course, CourseRequest request) {
        course.setName(request.getName());
        course.setDescription(request.getDescription());
    }

    public CourseResponse toResponse(Course course) {
        List<EnrollmentSummaryResponse> enrollments = course.getEnrollments().stream()
                .map(this::toEnrollmentSummary)
                .toList();

        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
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

