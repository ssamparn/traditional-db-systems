package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.entity.lifecycle;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthorBookReviewLifecycleIntegrationTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @AfterEach
    void cleanup() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void deletingAuthor_shouldDeleteBookAndReviewGraph() {
        Author author = new Author();
        author.setFirstName("Lifecycle");
        author.setLastName("Author");
        author.setEmail("lifecycle.author@example.com");

        Book book = new Book();
        book.setTitle("Lifecycle book");
        book.setIsbn("ISBN-LIFE-1");
        book.setDescription("lifecycle");
        book.setPublishedYear(2026);
        author.addBook(book);

        Review review = new Review();
        review.setRating(5);
        review.setComment("lifecycle review");
        review.setReviewerName("reviewer-life");
        book.addReview(review);

        Author saved = authorRepository.saveAndFlush(author);

        assertThat(bookRepository.count()).isEqualTo(1);
        assertThat(reviewRepository.count()).isEqualTo(1);

        authorRepository.delete(saved);
        authorRepository.flush();

        assertThat(authorRepository.count()).isZero();
        assertThat(bookRepository.count()).isZero();
        assertThat(reviewRepository.count()).isZero();
    }
}

