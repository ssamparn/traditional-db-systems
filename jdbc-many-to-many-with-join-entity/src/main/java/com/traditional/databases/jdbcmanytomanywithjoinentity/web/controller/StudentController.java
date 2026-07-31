package com.traditional.databases.jdbcmanytomanywithjoinentity.web.controller;

import com.traditional.databases.jdbcmanytomanywithjoinentity.service.StudentService;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request.StudentRequest;
import com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response.StudentResponse;
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
public class StudentController {

    private final StudentService studentService;

    @PostMapping("/student/create")
    public Mono<ResponseEntity<StudentResponse>> createStudent(@RequestBody StudentRequest request) {
        return studentService.createStudent(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/student/get/{studentId}")
    public Mono<ResponseEntity<StudentResponse>> getStudent(@PathVariable Long studentId) {
        return studentService.getStudentById(studentId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/student/get/all")
    public Flux<StudentResponse> getStudents() {
        return studentService.getAllStudents();
    }

    @PutMapping("/student/update/{studentId}")
    public Mono<ResponseEntity<StudentResponse>> updateStudent(@PathVariable Long studentId, @RequestBody StudentRequest request) {
        return studentService.updateStudent(studentId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/student/delete/{studentId}")
    public Mono<ResponseEntity<StudentResponse>> deleteStudent(@PathVariable Long studentId) {
        return studentService.deleteStudent(studentId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

