package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper.BookMapper;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.BookRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.BookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<BookResponse> createBook(BookRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateRequest(request);
                    String isbn = request.getIsbn().trim();
                    bookRepository.findByIsbn(isbn)
                            .ifPresent(existing -> {
                                throw new IllegalArgumentException("Book already exists with isbn: " + isbn);
                            });

                    Author author = findAuthorById(request.getAuthorId());
                    Book book = new Book();
                    book.setTitle(request.getTitle().trim());
                    book.setIsbn(isbn);
                    book.setDescription(trimOrNull(request.getDescription()));
                    book.setPublishedYear(request.getPublishedYear());
                    author.addBook(book);

                    Book saved = bookRepository.save(book);
                    return bookMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<BookResponse> updateBook(Long bookId, BookRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateRequest(request);
                    Book book = findBookByIdWithAuthorAndReviews(bookId);
                    String isbn = request.getIsbn().trim();
                    bookRepository.findByIsbn(isbn)
                            .filter(existing -> !existing.getId().equals(book.getId()))
                            .ifPresent(existing -> {
                                throw new IllegalArgumentException("Book already exists with isbn: " + isbn);
                            });

                    Author nextAuthor = findAuthorById(request.getAuthorId());
                    if (!book.getAuthor().getId().equals(nextAuthor.getId())) {
                        book.getAuthor().removeBook(book);
                        nextAuthor.addBook(book);
                    }

                    book.setTitle(request.getTitle().trim());
                    book.setIsbn(isbn);
                    book.setDescription(trimOrNull(request.getDescription()));
                    book.setPublishedYear(request.getPublishedYear());
                    Book saved = bookRepository.save(book);
                    return bookMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<BookResponse> getBookById(Long bookId) {
        return Mono.fromCallable(() -> bookMapper.toResponse(findBookByIdWithAuthorAndReviews(bookId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<BookResponse> getAllBooks() {
        return Mono.fromCallable(bookRepository::findAllWithAuthorAndReviews)
                .flatMapMany(Flux::fromIterable)
                .map(bookMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<BookResponse> deleteBook(Long bookId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Book book = findBookByIdWithAuthorAndReviews(bookId);
                    BookResponse response = bookMapper.toResponse(book);
                    book.getAuthor().removeBook(book);
                    bookRepository.delete(book);
                    return response;
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Book findBookByIdWithAuthorAndReviews(Long bookId) {
        return bookRepository.findByIdWithAuthorAndReviews(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with Id: " + bookId));
    }

    private Author findAuthorById(Long authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with Id: " + authorId));
    }

    private void validateRequest(BookRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        requireId(request.getAuthorId(), "authorId");
        requireText(request.getTitle(), "title", 160);
        requireText(request.getIsbn(), "isbn", 32);
        if (request.getPublishedYear() != null && request.getPublishedYear() < 1000) {
            throw new IllegalArgumentException("publishedYear must be >= 1000");
        }
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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

