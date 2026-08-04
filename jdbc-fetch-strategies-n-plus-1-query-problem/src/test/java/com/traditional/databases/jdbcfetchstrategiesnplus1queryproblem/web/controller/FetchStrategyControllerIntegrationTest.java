package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.controller;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FetchStrategyControllerIntegrationTest {

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
    void oneParent_lazyShouldIssueMoreQueriesThanJoinFetch() {
        Long authorId = seedData(1, 3, 2)[0];

        long lazyQueries = runScenario("/api/v1/fetch-demo/author/{authorId}/lazy", authorId);
        long joinFetchQueries = runScenario("/api/v1/fetch-demo/author/{authorId}/join-fetch", authorId);
        long entityGraphQueries = runScenario("/api/v1/fetch-demo/author/{authorId}/entity-graph", authorId);

        if (lazyQueries <= joinFetchQueries) {
            throw new AssertionError("Expected lazy queries to be greater than join fetch. lazy=" + lazyQueries + ", joinFetch=" + joinFetchQueries);
        }
        if (lazyQueries <= entityGraphQueries) {
            throw new AssertionError("Expected lazy queries to be greater than entity graph. lazy=" + lazyQueries + ", entityGraph=" + entityGraphQueries);
        }
    }

    @Test
    void manyParents_lazyShouldShowNPlus1PlusNExplosion() {
        seedData(3, 2, 2);

        long lazyQueries = runScenario("/api/v1/fetch-demo/authors/lazy", null);
        long joinFetchQueries = runScenario("/api/v1/fetch-demo/authors/join-fetch", null);
        long entityGraphQueries = runScenario("/api/v1/fetch-demo/authors/entity-graph", null);

        if (lazyQueries <= joinFetchQueries) {
            throw new AssertionError("Expected lazy query count to exceed join fetch. lazy=" + lazyQueries + ", joinFetch=" + joinFetchQueries);
        }
        if (lazyQueries <= entityGraphQueries) {
            throw new AssertionError("Expected lazy query count to exceed entity graph. lazy=" + lazyQueries + ", entityGraph=" + entityGraphQueries);
        }
    }

    @Test
    void manyBooks_eagerManyToOneShouldLoadAuthorsForEachBook() {
        seedData(2, 2, 1);

        webTestClient.get()
                .uri("/api/v1/fetch-demo/books/eager")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.scenario").isEqualTo("MANY_BOOKS_EAGER_MANY_TO_ONE")
                .jsonPath("$.bookCount").isEqualTo(4)
                .jsonPath("$.loadedAssociationCount").isEqualTo(4)
                .jsonPath("$.notes").exists();
    }

    private long runScenario(String uriTemplate, Long authorId) {
        WebTestClient.ResponseSpec responseSpec;
        if (authorId == null) {
            responseSpec = webTestClient.get()
                    .uri(uriTemplate)
                    .exchange();
        } else {
            responseSpec = webTestClient.get()
                    .uri(uriTemplate, authorId)
                    .exchange();
        }

        Object response = responseSpec
                .expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        Map<?, ?> responseMap = response instanceof Map<?, ?> map ? map : Map.of();
        Object queryCount = responseMap.get("queryCount");
        if (queryCount instanceof Integer value) {
            return value.longValue();
        }
        if (queryCount instanceof Long value) {
            return value;
        }
        throw new AssertionError("queryCount missing in fetch strategy response");
    }

    private Long[] seedData(int authorCount, int booksPerAuthor, int reviewsPerBook) {
        Long firstAuthorId = null;
        Long lastAuthorId = null;
        int bookIndex = 1;

        for (int i = 1; i <= authorCount; i++) {
            Author author = new Author();
            author.setFirstName("Author" + i);
            author.setLastName("Last" + i);
            author.setEmail("author" + i + "@fetch-demo.example.com");
            Author savedAuthor = authorRepository.saveAndFlush(author);
            if (firstAuthorId == null) {
                firstAuthorId = savedAuthor.getId();
            }
            lastAuthorId = savedAuthor.getId();

            for (int b = 1; b <= booksPerAuthor; b++) {
                Book book = new Book();
                book.setTitle("Book-" + bookIndex);
                book.setIsbn("ISBN-FETCH-" + bookIndex);
                book.setDescription("Demo book " + bookIndex);
                book.setPublishedYear(2020 + b);
                savedAuthor.addBook(book);
                Book savedBook = bookRepository.saveAndFlush(book);

                for (int r = 1; r <= reviewsPerBook; r++) {
                    Review review = new Review();
                    review.setRating(4);
                    review.setComment("Review " + r + " for book " + bookIndex);
                    review.setReviewerName("reviewer-" + r);
                    savedBook.addReview(review);
                    reviewRepository.saveAndFlush(review);
                }
                bookIndex++;
            }
        }
        return new Long[]{firstAuthorId, lastAuthorId};
    }
}

