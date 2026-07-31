package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Course;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.mapper.StudentMapper;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.StudentRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final StudentMapper studentMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<StudentResponse> createStudent(StudentRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateStudentRequest(request);
                    Student student = studentMapper.toEntity(request);
                    attachCoursesById(student, request.getCourseIds());
                    Student saved = studentRepository.save(student);
                    return studentMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<StudentResponse> updateStudent(Long studentId, StudentRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateStudentRequest(request);
                    Student student = findByIdWithCourses(studentId);
                    studentMapper.updateEntity(student, request);
                    resetCourses(student);
                    attachCoursesById(student, request.getCourseIds());
                    Student saved = studentRepository.save(student);
                    return studentMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<StudentResponse> deleteStudent(Long studentId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Student student = findByIdWithCourses(studentId);
                    resetCourses(student);
                    studentRepository.delete(student);
                    return studentMapper.toResponse(student);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<StudentResponse> getAllStudents() {
        return Flux.fromIterable(studentRepository.findAllWithCourses())
                .map(studentMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<StudentResponse> getStudentById(Long studentId) {
        return Mono.fromCallable(() -> {
                    Student student = findByIdWithCourses(studentId);
                    return studentMapper.toResponse(student);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Student findByIdWithCourses(Long studentId) {
        return studentRepository.findByIdWithCourses(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with Id: " + studentId));
    }

    private void attachCoursesById(Student student, List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return;
        }
        List<Course> courses = courseRepository.findAllById(courseIds);
        courses.forEach(student::addCourse);
    }

    private void resetCourses(Student student) {
        List<Course> existingCourses = List.copyOf(student.getCourses());
        existingCourses.forEach(student::removeCourse);
    }

    private void validateStudentRequest(StudentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        requireText(request.getFirstName(), "firstName", 80);
        requireText(request.getLastName(), "lastName", 80);
        requireText(request.getMobile(), "mobile", 20);
        requireText(request.getEmail(), "email", 128);
        if (!request.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("email must be a valid email address");
        }
    }

    private void requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxLength + " characters");
        }
    }

    private <T> T inTransaction(Supplier<T> supplier) {
        T result = transactionTemplate.execute(status -> supplier.get());
        if (result == null) {
            throw new IllegalStateException("Transaction returned null result");
        }
        return result;
    }
}


