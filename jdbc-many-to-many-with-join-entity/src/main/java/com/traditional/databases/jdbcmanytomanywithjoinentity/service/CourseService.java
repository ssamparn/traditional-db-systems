package com.traditional.databases.jdbcmanytomanywithjoinentity.service;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.mapper.CourseMapper;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.CourseRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.CourseResponse;
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
    private final CourseMapper courseMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<CourseResponse> createCourse(CourseRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateCourseRequest(request);
                    Course course = courseMapper.toEntity(request);
                    Course saved = courseRepository.save(course);
                    return courseMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<CourseResponse> updateCourse(Long courseId, CourseRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateCourseRequest(request);
                    Course course = findByIdWithEnrollments(courseId);
                    courseMapper.updateEntity(course, request);
                    Course saved = courseRepository.save(course);
                    return courseMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<CourseResponse> deleteCourse(Long courseId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Course course = findByIdWithEnrollments(courseId);
                    clearEnrollments(course);
                    courseRepository.delete(course);
                    return courseMapper.toResponse(course);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<CourseResponse> getAllCourses() {
        return Flux.fromIterable(courseRepository.findAllWithEnrollments())
                .map(courseMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<CourseResponse> getCourseById(Long courseId) {
        return Mono.fromCallable(() -> courseMapper.toResponse(findByIdWithEnrollments(courseId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Course findByIdWithEnrollments(Long courseId) {
        return courseRepository.findByIdWithEnrollments(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with Id: " + courseId));
    }

    private void clearEnrollments(Course course) {
        List<Enrollment> existing = List.copyOf(course.getEnrollments());
        existing.forEach(enrollment -> {
            if (enrollment.getStudent() != null) {
                enrollment.getStudent().removeEnrollment(enrollment);
            }
            course.removeEnrollment(enrollment);
        });
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

