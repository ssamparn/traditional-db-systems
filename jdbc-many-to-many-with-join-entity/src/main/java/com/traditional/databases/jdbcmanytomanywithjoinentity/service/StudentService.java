package com.traditional.databases.jdbcmanytomanywithjoinentity.service;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Enrollment;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.Student;
import com.traditional.databases.jdbcmanytomanywithjoinentity.db.repository.StudentRepository;
import com.traditional.databases.jdbcmanytomanywithjoinentity.mapper.StudentMapper;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.StudentRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.StudentResponse;
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
    private final StudentMapper studentMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<StudentResponse> createStudent(StudentRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateStudentRequest(request);
                    Student student = studentMapper.toEntity(request);
                    Student saved = studentRepository.save(student);
                    return studentMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<StudentResponse> updateStudent(Long studentId, StudentRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateStudentRequest(request);
                    Student student = findByIdWithEnrollments(studentId);
                    studentMapper.updateEntity(student, request);
                    Student saved = studentRepository.save(student);
                    return studentMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<StudentResponse> deleteStudent(Long studentId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Student student = findByIdWithEnrollments(studentId);
                    clearEnrollments(student);
                    studentRepository.delete(student);
                    return studentMapper.toResponse(student);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<StudentResponse> getAllStudents() {
        return Flux.fromIterable(studentRepository.findAllWithEnrollments())
                .map(studentMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<StudentResponse> getStudentById(Long studentId) {
        return Mono.fromCallable(() -> studentMapper.toResponse(findByIdWithEnrollments(studentId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Student findByIdWithEnrollments(Long studentId) {
        return studentRepository.findByIdWithEnrollments(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with Id: " + studentId));
    }

    private void clearEnrollments(Student student) {
        List<Enrollment> existing = List.copyOf(student.getEnrollments());
        existing.forEach(enrollment -> {
            if (enrollment.getCourse() != null) {
                enrollment.getCourse().removeEnrollment(enrollment);
            }
            student.removeEnrollment(enrollment);
        });
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

