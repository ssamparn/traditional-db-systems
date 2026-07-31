package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.mapper;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Course;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.CourseRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.StudentRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.CourseResponse;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.CourseSummaryResponse;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.StudentResponse;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.StudentSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RelationMapper {

    public Student toStudentEntity(StudentRequest request) {
        Student student = new Student();
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setMobile(request.getMobile());
        student.setEmail(request.getEmail());
        return student;
    }

    public Course toCourseEntity(CourseRequest request) {
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        return course;
    }

    public void updateStudentEntity(Student student, StudentRequest request) {
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setMobile(request.getMobile());
        student.setEmail(request.getEmail());
    }

    public void updateCourseEntity(Course course, CourseRequest request) {
        course.setName(request.getName());
        course.setDescription(request.getDescription());
    }

    public StudentResponse toStudentResponse(Student student) {
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

    public CourseResponse toCourseResponse(Course course) {
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

