package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.mapper;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.StudentRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.CourseSummaryResponse;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.StudentResponse;
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
        List<CourseSummaryResponse> courses = student.getCourses().stream()
                .map(course -> new CourseSummaryResponse(course.getId(), course.getName(), course.getDescription()))
                .toList();

        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getMobile(),
                student.getEmail(),
                courses
        );
    }
}


