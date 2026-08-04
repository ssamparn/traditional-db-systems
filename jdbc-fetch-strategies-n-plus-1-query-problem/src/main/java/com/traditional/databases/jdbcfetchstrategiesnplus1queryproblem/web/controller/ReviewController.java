package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.controller;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service.ReviewService;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.ReviewRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.ReviewUpdateRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.ReviewResponse;
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
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/review/create")
    public Mono<ResponseEntity<ReviewResponse>> createReview(@RequestBody ReviewRequest request) {
        return reviewService.createReview(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/review/get/{reviewId}")
    public Mono<ResponseEntity<ReviewResponse>> getReview(@PathVariable Long reviewId) {
        return reviewService.getReviewById(reviewId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/review/get/by-book/{bookId}")
    public Flux<ReviewResponse> getReviewsByBook(@PathVariable Long bookId) {
        return reviewService.getReviewsByBookId(bookId);
    }

    @PutMapping("/review/update/{reviewId}")
    public Mono<ResponseEntity<ReviewResponse>> updateReview(@PathVariable Long reviewId,
                                                             @RequestBody ReviewUpdateRequest request) {
        return reviewService.updateReview(reviewId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/review/delete/{reviewId}")
    public Mono<ResponseEntity<ReviewResponse>> deleteReview(@PathVariable Long reviewId) {
        return reviewService.deleteReview(reviewId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

