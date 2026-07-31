package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Course;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.entity.Student;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.mapper.CourseMapper;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.CourseRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.CourseResponse;
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
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final CourseMapper courseMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<CourseResponse> createCourse(CourseRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateCourseRequest(request);
                    Course course = courseMapper.toEntity(request);
                    attachStudentsById(course, request.getStudentIds());
                    Course saved = courseRepository.save(course);
                    return courseMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<CourseResponse> updateCourse(Long courseId, CourseRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateCourseRequest(request);
                    Course course = findByIdWithStudents(courseId);
                    courseMapper.updateEntity(course, request);
                    resetStudents(course);
                    attachStudentsById(course, request.getStudentIds());
                    Course saved = courseRepository.save(course);
                    return courseMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<CourseResponse> deleteCourse(Long courseId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Course course = findByIdWithStudents(courseId);
                    resetStudents(course);
                    courseRepository.delete(course);
                    return courseMapper.toResponse(course);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<CourseResponse> getAllCourses() {
        return Flux.fromIterable(courseRepository.findAllWithStudents())
                .map(courseMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<CourseResponse> getCourseById(Long courseId) {
        return Mono.fromCallable(() -> {
                    Course course = findByIdWithStudents(courseId);
                    return courseMapper.toResponse(course);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Course findByIdWithStudents(Long courseId) {
        return courseRepository.findByIdWithStudents(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with Id: " + courseId));
    }

    private void attachStudentsById(Course course, List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }
        List<Student> students = studentRepository.findAllById(studentIds);
        // Student is the owning side of the relationship and must drive join-table writes.
        students.forEach(student -> student.addCourse(course));
    }

    private void resetStudents(Course course) {
        List<Student> existingStudents = List.copyOf(course.getStudents());
        // Remove from owning side so old join rows are deleted reliably.
        existingStudents.forEach(student -> student.removeCourse(course));
    }

    private void validateCourseRequest(CourseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        requireText(request.getName(), "name", 80);
        requireText(request.getDescription(), "description", 240);
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



