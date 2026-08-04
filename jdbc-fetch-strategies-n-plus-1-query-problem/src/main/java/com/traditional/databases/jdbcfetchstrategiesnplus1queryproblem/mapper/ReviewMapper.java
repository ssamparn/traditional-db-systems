package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.BookSummaryResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.ReviewResponse;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewerName(),
                new BookSummaryResponse(
                        review.getBook().getId(),
                        review.getBook().getTitle(),
                        review.getBook().getIsbn(),
                        review.getBook().getPublishedYear()
                )
        );
    }
}

