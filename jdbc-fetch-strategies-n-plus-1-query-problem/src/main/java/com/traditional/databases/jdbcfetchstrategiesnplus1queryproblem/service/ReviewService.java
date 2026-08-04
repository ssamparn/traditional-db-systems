package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper.ReviewMapper;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.ReviewRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.ReviewUpdateRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.ReviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final ReviewMapper reviewMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<ReviewResponse> createReview(ReviewRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateCreateRequest(request);
                    Book book = findBookById(request.getBookId());

                    Review review = new Review();
                    review.setRating(request.getRating());
                    review.setComment(request.getComment().trim());
                    review.setReviewerName(request.getReviewerName().trim());
                    book.addReview(review);

                    Review saved = reviewRepository.save(review);
                    return reviewMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ReviewResponse> updateReview(Long reviewId, ReviewUpdateRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateUpdateRequest(request);
                    Review review = findReviewByIdWithBookAndAuthor(reviewId);
                    review.setRating(request.getRating());
                    review.setComment(request.getComment().trim());
                    review.setReviewerName(request.getReviewerName().trim());
                    Review saved = reviewRepository.save(review);
                    return reviewMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ReviewResponse> getReviewById(Long reviewId) {
        return Mono.fromCallable(() -> reviewMapper.toResponse(findReviewByIdWithBookAndAuthor(reviewId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<ReviewResponse> getReviewsByBookId(Long bookId) {
        return Mono.fromCallable(() -> reviewRepository.findAllByBookId(bookId))
                .flatMapMany(Flux::fromIterable)
                .map(reviewMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ReviewResponse> deleteReview(Long reviewId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Review review = findReviewByIdWithBookAndAuthor(reviewId);
                    ReviewResponse response = reviewMapper.toResponse(review);
                    review.getBook().removeReview(review);
                    reviewRepository.delete(review);
                    return response;
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Review findReviewByIdWithBookAndAuthor(Long reviewId) {
        return reviewRepository.findByIdWithBookAndAuthor(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with Id: " + reviewId));
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with Id: " + bookId));
    }

    private void validateCreateRequest(ReviewRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        requireId(request.getBookId(), "bookId");
        validateRating(request.getRating());
        requireText(request.getComment(), "comment", 1200);
        requireText(request.getReviewerName(), "reviewerName", 100);
    }

    private void validateUpdateRequest(ReviewUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        validateRating(request.getRating());
        requireText(request.getComment(), "comment", 1200);
        requireText(request.getReviewerName(), "reviewerName", 100);
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
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

