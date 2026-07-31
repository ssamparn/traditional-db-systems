package com.traditional.databases.jdbcmanytomanywithjoinentity.web.controller;

import com.traditional.databases.jdbcmanytomanywithjoinentity.service.EnrollmentService;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.EnrollmentRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.EnrollmentUpdateRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.EnrollmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enrollment/create")
    public Mono<ResponseEntity<EnrollmentResponse>> createEnrollment(@RequestBody EnrollmentRequest request) {
        return enrollmentService.createEnrollment(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/enrollment/get/{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponse>> getEnrollment(@PathVariable Long enrollmentId) {
        return enrollmentService.getEnrollmentById(enrollmentId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/enrollment/get/all")
    public Flux<EnrollmentResponse> getEnrollments() {
        return enrollmentService.getAllEnrollments();
    }

    @GetMapping("/enrollment/get/by-student/{studentId}")
    public Flux<EnrollmentResponse> getEnrollmentsByStudent(@PathVariable Long studentId) {
        return enrollmentService.getEnrollmentsByStudentId(studentId);
    }

    @GetMapping("/enrollment/get/by-course/{courseId}")
    public Flux<EnrollmentResponse> getEnrollmentsByCourse(@PathVariable Long courseId) {
        return enrollmentService.getEnrollmentsByCourseId(courseId);
    }

    @PutMapping("/enrollment/update/{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponse>> updateEnrollment(@PathVariable Long enrollmentId,
                                                                     @RequestBody EnrollmentUpdateRequest request) {
        return enrollmentService.updateEnrollment(enrollmentId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/enrollment/delete/{enrollmentId}")
    public Mono<ResponseEntity<EnrollmentResponse>> deleteEnrollment(@PathVariable Long enrollmentId) {
        return enrollmentService.deleteEnrollment(enrollmentId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

