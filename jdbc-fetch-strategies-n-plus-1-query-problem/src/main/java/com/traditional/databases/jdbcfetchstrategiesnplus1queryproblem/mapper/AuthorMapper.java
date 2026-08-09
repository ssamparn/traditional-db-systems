package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.AuthorResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.AuthorSummaryResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.BookSummaryResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.FetchStrategyReportResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.ReviewSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@Component
public class AuthorMapper {

    public AuthorResponse toResponse(Author author) {
        List<Book> books = uniqueBooks(author);
        return new AuthorResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getEmail(),
                books.stream()
                        .sorted(Comparator.comparing(Book::getId))
                        .map(this::toBookSummary)
                        .toList()
        );
    }

    public AuthorSummaryResponse toSummary(Author author) {
        return new AuthorSummaryResponse(author.getId(), author.getFirstName() + " " + author.getLastName(), author.getEmail());
    }

    public FetchStrategyReportResponse.FetchAuthorGraphResponse toFetchGraph(Author author) {
        List<Book> books = uniqueBooks(author);
        return new FetchStrategyReportResponse.FetchAuthorGraphResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getEmail(),
                books.stream()
                        .sorted(Comparator.comparing(Book::getId))
                        .map(this::toFetchBookGraphWithoutReviews)
                        .toList()
        );
    }

    public FetchStrategyReportResponse.FetchAuthorGraphResponse toFetchGraphWithReviews(Author author) {
        List<Book> books = uniqueBooks(author);
        return new FetchStrategyReportResponse.FetchAuthorGraphResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getEmail(),
                books.stream()
                        .sorted(Comparator.comparing(Book::getId))
                        .map(this::toFetchBookGraphWithReviews)
                        .toList()
        );
    }

    public int countBooks(Author author) {
        return uniqueBooks(author).size();
    }

    public int countReviews(Author author) {
        return uniqueBooks(author).stream().mapToInt(book -> book.getReviews().size()).sum();
    }

    public long countLoadedAuthorsOnBooks(List<Author> authors) {
        return authors.stream()
                .flatMap(author -> uniqueBooks(author).stream())
                .map(Book::getAuthor)
                .filter(Objects::nonNull)
                .count();
    }

    private List<Book> uniqueBooks(Author author) {
        LinkedHashMap<Long, Book> booksById = new LinkedHashMap<>();
        for (Book book : author.getBooks()) {
            if (book == null) {
                continue;
            }
            Long key = book.getId();
            if (key == null) {
                key = Long.MIN_VALUE + booksById.size();
            }
            booksById.putIfAbsent(key, book);
        }
        return new ArrayList<>(booksById.values());
    }

    private BookSummaryResponse toBookSummary(Book book) {
        return new BookSummaryResponse(book.getId(), book.getTitle(), book.getIsbn(), book.getPublishedYear());
    }

    private FetchStrategyReportResponse.FetchBookGraphResponse toFetchBookGraphWithoutReviews(Book book) {
        return new FetchStrategyReportResponse.FetchBookGraphResponse(book.getId(), book.getTitle(), book.getIsbn(), book.getPublishedYear(), List.of());
    }

    private FetchStrategyReportResponse.FetchBookGraphResponse toFetchBookGraphWithReviews(Book book) {
        return new FetchStrategyReportResponse.FetchBookGraphResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPublishedYear(),
                book.getReviews()
                        .stream()
                        .sorted(Comparator.comparing(Review::getId))
                        .map(review -> new ReviewSummaryResponse(review.getId(), review.getRating(), review.getReviewerName()))
                        .toList()
        );
    }
}

