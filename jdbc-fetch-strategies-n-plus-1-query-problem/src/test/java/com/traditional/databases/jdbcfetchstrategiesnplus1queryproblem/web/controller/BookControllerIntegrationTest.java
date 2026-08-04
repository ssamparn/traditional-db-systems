package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.controller;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.BookRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setupClient() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanup() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void createBook_shouldPersistAndReturnCreated() {
        Author author = authorRepository.saveAndFlush(createAuthor("Kent", "Beck", "kent.beck@example.com"));

        webTestClient.post()
                .uri("/api/v1/book/create")
                .bodyValue(new BookRequest("TDD by example", "ISBN-BOOK-1", "core tdd", 2002, author.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.author.id").isEqualTo(author.getId())
                .jsonPath("$.reviews.length()").isEqualTo(0);
    }

    @Test
    void getBook_withExistingId_shouldReturnBookWithReviews() {
        Author author = authorRepository.saveAndFlush(createAuthor("Gregor", "Hohpe", "gregor.hohpe@example.com"));
        Book book = createBook("Enterprise integration", "ISBN-BOOK-2", author);
        author.addBook(book);
        Book savedBook = bookRepository.saveAndFlush(book);
        Review review = createReview(savedBook, 5, "Great patterns", "reader-c");
        savedBook.addReview(review);
        reviewRepository.saveAndFlush(review);

        webTestClient.get()
                .uri("/api/v1/book/get/{bookId}", savedBook.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedBook.getId())
                .jsonPath("$.reviews.length()").isEqualTo(1)
                .jsonPath("$.reviews[0].reviewerName").isEqualTo("reader-c");
    }

    @Test
    void updateBook_shouldMoveToAnotherAuthor() {
        Author firstAuthor = authorRepository.saveAndFlush(createAuthor("Author", "One", "author.one@example.com"));
        Author secondAuthor = authorRepository.saveAndFlush(createAuthor("Author", "Two", "author.two@example.com"));
        Book book = createBook("Refactoring", "ISBN-BOOK-3", firstAuthor);
        firstAuthor.addBook(book);
        Book savedBook = bookRepository.saveAndFlush(book);

        webTestClient.put()
                .uri("/api/v1/book/update/{bookId}", savedBook.getId())
                .bodyValue(new BookRequest("Refactoring 2", "ISBN-BOOK-3", "updated", 2018, secondAuthor.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedBook.getId())
                .jsonPath("$.author.id").isEqualTo(secondAuthor.getId())
                .jsonPath("$.title").isEqualTo("Refactoring 2");
    }

    @Test
    void createBook_withUnknownAuthor_shouldReturnNotFound() {
        webTestClient.post()
                .uri("/api/v1/book/create")
                .bodyValue(new BookRequest("Unknown author", "ISBN-BOOK-4", "desc", 2026, 99999L))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Author not found with Id: 99999");
    }

    private Author createAuthor(String firstName, String lastName, String email) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setEmail(email);
        return author;
    }

    private Book createBook(String title, String isbn, Author author) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription("desc");
        book.setPublishedYear(2020);
        book.assignAuthor(author);
        return book;
    }

    private Review createReview(Book book, Integer rating, String comment, String reviewerName) {
        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewerName(reviewerName);
        review.assignBook(book);
        return review;
    }
}

