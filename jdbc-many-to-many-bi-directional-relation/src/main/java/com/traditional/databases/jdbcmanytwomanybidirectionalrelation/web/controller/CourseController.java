package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.controller;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service.CourseService;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.CourseRequest;
import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.response.CourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/course/create")
    public Mono<ResponseEntity<CourseResponse>> createCourse(@RequestBody CourseRequest courseRequest) {
        return courseService.createCourse(courseRequest)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @DeleteMapping("/course/delete/{courseId}")
    public Mono<ResponseEntity<CourseResponse>> deleteCourse(@PathVariable Long courseId) {
        return courseService.deleteCourse(courseId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/course/get/{courseId}")
    public Mono<ResponseEntity<CourseResponse>> getCourse(@PathVariable Long courseId) {
        return courseService.getCourseById(courseId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/course/get/all")
    public Flux<CourseResponse> getCourses() {
        return courseService.getAllCourses();
    }

    @PutMapping("/course/update/{courseId}")
    public Mono<ResponseEntity<CourseResponse>> updateCourse(@PathVariable Long courseId, @RequestBody CourseRequest courseRequest) {
        return courseService.updateCourse(courseId, courseRequest)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

