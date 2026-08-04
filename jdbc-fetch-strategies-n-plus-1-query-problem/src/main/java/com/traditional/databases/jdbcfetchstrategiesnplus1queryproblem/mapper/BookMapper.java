package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.BookResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.ReviewSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class BookMapper {

    private final AuthorMapper authorMapper;

    public BookMapper(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getDescription(),
                book.getPublishedYear(),
                authorMapper.toSummary(book.getAuthor()),
                book.getReviews().stream()
                        .sorted(Comparator.comparing(Review::getId))
                        .map(review -> new ReviewSummaryResponse(review.getId(), review.getRating(), review.getReviewerName()))
                        .toList()
        );
    }
}

