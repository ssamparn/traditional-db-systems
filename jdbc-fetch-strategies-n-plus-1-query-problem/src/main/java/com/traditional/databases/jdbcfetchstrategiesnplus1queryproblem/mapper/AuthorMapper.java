package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.AuthorResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.AuthorSummaryResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.BookSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class AuthorMapper {

    public AuthorResponse toResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getEmail(),
                author.getBooks()
                        .stream()
                        .sorted(Comparator.comparing(Book::getId))
                        .map(this::toBookSummary)
                        .toList()
        );
    }

    public AuthorSummaryResponse toSummary(Author author) {
        return new AuthorSummaryResponse(author.getId(), author.getFirstName() + " " + author.getLastName(), author.getEmail());
    }

    public int countBooks(Author author) {
        return author.getBooks().size();
    }

    public int countReviews(Author author) {
        return author.getBooks().stream().mapToInt(book -> book.getReviews().size()).sum();
    }

    public long countLoadedAuthorsOnBooks(List<Author> authors) {
        return authors.stream()
                .flatMap(author -> author.getBooks().stream())
                .map(Book::getAuthor)
                .filter(Objects::nonNull)
                .count();
    }

    private BookSummaryResponse toBookSummary(Book book) {
        return new BookSummaryResponse(book.getId(), book.getTitle(), book.getIsbn(), book.getPublishedYear());
    }
}

