package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.mapper;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Course;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.CourseRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.CourseResponse;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.StudentSummaryResponse;
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
        List<StudentSummaryResponse> students = course.getStudents().stream()
                .map(student -> new StudentSummaryResponse(student.getId(), student.getFirstName(), student.getLastName(), student.getEmail()))
                .toList();

        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                students
        );
    }
}


