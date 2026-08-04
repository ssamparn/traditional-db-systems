package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.controller;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.AuthorRequest;
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
class AuthorControllerIntegrationTest {

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
    void createAuthor_shouldPersistAndReturnCreated() {
        webTestClient.post()
                .uri("/api/v1/author/create")
                .bodyValue(new AuthorRequest("Martin", "Fowler", "martin.fowler@example.com"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.firstName").isEqualTo("Martin")
                .jsonPath("$.books.length()").isEqualTo(0);
    }

    @Test
    void getAuthor_withExistingId_shouldReturnBooks() {
        Author author = authorRepository.saveAndFlush(createAuthor("Neal", "Ford", "neal.ford@example.com"));
        Book book = createBook("Software architecture", "ISBN-AUTH-1", author);
        author.addBook(book);
        Book savedBook = bookRepository.saveAndFlush(book);
        Review review = createReview(5, "Essential read", "reader-a", savedBook);
        savedBook.addReview(review);
        reviewRepository.saveAndFlush(review);

        webTestClient.get()
                .uri("/api/v1/author/get/{authorId}", author.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(author.getId())
                .jsonPath("$.books.length()").isEqualTo(1)
                .jsonPath("$.books[0].title").isEqualTo("Software architecture");
    }

    @Test
    void createAuthor_withDuplicateEmail_shouldReturnBadRequest() {
        authorRepository.saveAndFlush(createAuthor("Sam", "Newman", "sam.newman@example.com"));

        webTestClient.post()
                .uri("/api/v1/author/create")
                .bodyValue(new AuthorRequest("Sam", "Newman", "sam.newman@example.com"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Author already exists with email: sam.newman@example.com");
    }

    @Test
    void deleteAuthor_shouldRemoveBooksAndReviews() {
        Author author = authorRepository.saveAndFlush(createAuthor("Eric", "Evans", "eric.evans@example.com"));
        Book book = createBook("Domain-driven design", "ISBN-AUTH-2", author);
        author.addBook(book);
        Book savedBook = bookRepository.saveAndFlush(book);
        Review review = createReview(4, "Still relevant", "reader-b", savedBook);
        savedBook.addReview(review);
        reviewRepository.saveAndFlush(review);

        webTestClient.delete()
                .uri("/api/v1/author/delete/{authorId}", author.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(author.getId());

        webTestClient.get()
                .uri("/api/v1/book/get/{bookId}", savedBook.getId())
                .exchange()
                .expectStatus().isNotFound();
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
        book.setPublishedYear(2024);
        book.assignAuthor(author);
        return book;
    }

    private Review createReview(Integer rating, String comment, String reviewerName, Book book) {
        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewerName(reviewerName);
        review.assignBook(book);
        return review;
    }
}

