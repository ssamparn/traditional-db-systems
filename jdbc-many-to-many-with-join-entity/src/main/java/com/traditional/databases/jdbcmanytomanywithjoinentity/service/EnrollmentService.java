package com.traditional.databases.jdbcmanytomanywithjoinentity.service;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Course;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Student;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.CourseRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.EnrollmentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.mapper.EnrollmentMapper;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.EnrollmentRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.EnrollmentUpdateRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.EnrollmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<EnrollmentResponse> createEnrollment(EnrollmentRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateCreateRequest(request);
                    Student student = findStudentById(request.getStudentId());
                    Course course = findCourseById(request.getCourseId());

                    if (enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId()).isPresent()) {
                        throw new IllegalArgumentException("Enrollment already exists for studentId=" + student.getId() + " and courseId=" + course.getId());
                    }

                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrolledAt(resolveEnrolledAt(request.getEnrolledAt()));
                    enrollment.setStatus(request.getStatus());
                    enrollment.setGrade(request.getGrade());
                    enrollment.setCreatedBy(request.getCreatedBy().trim());

                    student.addEnrollment(enrollment);
                    course.addEnrollment(enrollment);

                    Enrollment saved = enrollmentRepository.save(enrollment);
                    return enrollmentMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<EnrollmentResponse> updateEnrollment(Long enrollmentId, EnrollmentUpdateRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateUpdateRequest(request);
                    Enrollment enrollment = findEnrollmentById(enrollmentId);
                    enrollment.setEnrolledAt(resolveEnrolledAt(request.getEnrolledAt()));
                    enrollment.setStatus(request.getStatus());
                    enrollment.setGrade(request.getGrade());
                    enrollment.setCreatedBy(request.getCreatedBy().trim());
                    Enrollment saved = enrollmentRepository.save(enrollment);
                    return enrollmentMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<EnrollmentResponse> getEnrollmentById(Long enrollmentId) {
        return Mono.fromCallable(() -> enrollmentMapper.toResponse(findEnrollmentById(enrollmentId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<EnrollmentResponse> getAllEnrollments() {
        return Flux.fromIterable(enrollmentRepository.findAllWithStudentAndCourse())
                .map(enrollmentMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<EnrollmentResponse> getEnrollmentsByStudentId(Long studentId) {
        return Flux.fromIterable(enrollmentRepository.findAllByStudentIdWithStudentAndCourse(studentId))
                .map(enrollmentMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<EnrollmentResponse> getEnrollmentsByCourseId(Long courseId) {
        return Flux.fromIterable(enrollmentRepository.findAllByCourseIdWithStudentAndCourse(courseId))
                .map(enrollmentMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<EnrollmentResponse> deleteEnrollment(Long enrollmentId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Enrollment enrollment = findEnrollmentById(enrollmentId);
                    EnrollmentResponse response = enrollmentMapper.toResponse(enrollment);
                    enrollment.getStudent().removeEnrollment(enrollment);
                    enrollment.getCourse().removeEnrollment(enrollment);
                    enrollmentRepository.delete(enrollment);
                    return response;
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Enrollment findEnrollmentById(Long enrollmentId) {
        return enrollmentRepository.findByIdWithStudentAndCourse(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with Id: " + enrollmentId));
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with Id: " + studentId));
    }

    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with Id: " + courseId));
    }

    private Instant resolveEnrolledAt(Instant enrolledAt) {
        return enrolledAt == null ? Instant.now() : enrolledAt;
    }

    private void validateCreateRequest(EnrollmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        requireId(request.getStudentId(), "studentId");
        requireId(request.getCourseId(), "courseId");
        requireText(request.getCreatedBy(), "createdBy", 100);
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }
    }

    private void validateUpdateRequest(EnrollmentUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        requireText(request.getCreatedBy(), "createdBy", 100);
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }
    }

    private void requireId(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be a positive number");
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

