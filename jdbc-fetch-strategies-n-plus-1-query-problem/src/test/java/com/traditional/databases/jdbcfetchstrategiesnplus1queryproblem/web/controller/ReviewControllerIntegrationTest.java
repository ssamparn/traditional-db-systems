package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.controller;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.ReviewRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.ReviewUpdateRequest;
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
class ReviewControllerIntegrationTest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

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
    void createReview_shouldPersistAndReturnCreated() {
        Book book = seedBook("ISBN-REVIEW-1", "Microservices patterns", "Susan", "Fowler", "susan.fowler@example.com");

        webTestClient.post()
                .uri("/api/v1/review/create")
                .bodyValue(new ReviewRequest(book.getId(), 5, "High signal", "reader-x"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.book.id").isEqualTo(book.getId())
                .jsonPath("$.rating").isEqualTo(5);
    }

    @Test
    void updateReview_shouldChangeCommentAndRating() {
        Book book = seedBook("ISBN-REVIEW-2", "Designing data-intensive apps", "Martin", "Kleppmann", "martin.k@example.com");
        Review review = createReview(book, 3, "good", "reader-y");
        book.addReview(review);
        Review saved = reviewRepository.saveAndFlush(review);

        webTestClient.put()
                .uri("/api/v1/review/update/{reviewId}", saved.getId())
                .bodyValue(new ReviewUpdateRequest(5, "excellent", "reader-y"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(saved.getId())
                .jsonPath("$.rating").isEqualTo(5)
                .jsonPath("$.comment").isEqualTo("excellent");
    }

    @Test
    void getReviewsByBook_shouldReturnOnlyBookReviews() {
        Book firstBook = seedBook("ISBN-REVIEW-3", "Book 1", "First", "Author", "first.author@example.com");
        Book secondBook = seedBook("ISBN-REVIEW-4", "Book 2", "Second", "Author", "second.author@example.com");

        Review first = createReview(firstBook, 4, "solid", "reader-1");
        firstBook.addReview(first);
        reviewRepository.saveAndFlush(first);

        Review second = createReview(firstBook, 5, "great", "reader-2");
        firstBook.addReview(second);
        reviewRepository.saveAndFlush(second);

        Review third = createReview(secondBook, 2, "weak", "reader-3");
        secondBook.addReview(third);
        reviewRepository.saveAndFlush(third);

        webTestClient.get()
                .uri("/api/v1/review/get/by-book/{bookId}", firstBook.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].book.id").isEqualTo(firstBook.getId())
                .jsonPath("$[1].book.id").isEqualTo(firstBook.getId());
    }

    @Test
    void deleteReview_shouldKeepBook() {
        Book book = seedBook("ISBN-REVIEW-5", "Resilient systems", "Uwe", "Friedrichsen", "uwe.f@example.com");
        Review review = createReview(book, 4, "helpful", "reader-z");
        book.addReview(review);
        Review saved = reviewRepository.saveAndFlush(review);

        webTestClient.delete()
                .uri("/api/v1/review/delete/{reviewId}", saved.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(saved.getId());

        webTestClient.get()
                .uri("/api/v1/book/get/{bookId}", book.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(book.getId())
                .jsonPath("$.reviews.length()").isEqualTo(0);
    }

    private Book seedBook(String isbn, String title, String firstName, String lastName, String email) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setEmail(email);
        Author savedAuthor = authorRepository.saveAndFlush(author);

        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription("desc");
        book.setPublishedYear(2024);
        savedAuthor.addBook(book);
        return bookRepository.saveAndFlush(book);
    }

    private Review createReview(Book book, int rating, String comment, String reviewerName) {
        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewerName(reviewerName);
        review.assignBook(book);
        return review;
    }
}

