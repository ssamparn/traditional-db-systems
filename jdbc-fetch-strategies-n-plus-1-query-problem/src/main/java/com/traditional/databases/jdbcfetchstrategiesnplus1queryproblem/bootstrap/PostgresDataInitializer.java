package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.bootstrap;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Profile("postgres")
public class PostgresDataInitializer implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public PostgresDataInitializer(
            AuthorRepository authorRepository,
            BookRepository bookRepository,
            ReviewRepository reviewRepository
    ) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        if (authorRepository.count() > 0 || bookRepository.count() > 0 || reviewRepository.count() > 0) {
            log.info("Skipping postgres seed. Existing data found in authors/books/reviews tables.");
            return;
        }

        Author authorOne = createAuthor("Martin", "Fowler", "martin.fowler@example.com");
        Author authorTwo = createAuthor("Rebecca", "Parsons", "rebecca.parsons@example.com");
        Author authorThree = createAuthor("Neal", "Ford", "neal.ford@example.com");

        List<Author> authors = List.of(authorOne, authorTwo, authorThree);
        List<String> reviewerNames = List.of("Anita", "Rahul", "Maria", "Sofia", "Ken");

        int isbnCounter = 1;
        for (int authorIndex = 0; authorIndex < authors.size(); authorIndex++) {
            Author author = authors.get(authorIndex);
            for (int bookIndex = 1; bookIndex <= 3; bookIndex++) {
                Book book = createBook(
                        String.format("%s: Volume %d", author.getLastName(), bookIndex),
                        String.format("ISBN-978000000%03d", isbnCounter++),
                        String.format("Seeded book %d for %s %s.", bookIndex, author.getFirstName(), author.getLastName()),
                        2018 + authorIndex + bookIndex
                );
                author.addBook(book);

                for (int reviewIndex = 0; reviewIndex < 5; reviewIndex++) {
                    int rating = (reviewIndex % 2 == 0) ? 5 : 4;
                    Review review = createReview(
                            rating,
                            String.format("Review %d for %s.", reviewIndex + 1, book.getTitle()),
                            reviewerNames.get(reviewIndex)
                    );
                    book.addReview(review);
                }
            }
        }

        authorRepository.saveAll(authors);

        log.info("Postgres seed complete: inserted {} authors, {} books, {} reviews.",
                authors.size(),
                authors.stream().mapToInt(author -> author.getBooks().size()).sum(),
                authors.stream().flatMap(author -> author.getBooks().stream()).mapToInt(book -> book.getReviews().size()).sum());
    }

    private static Author createAuthor(String firstName, String lastName, String email) {
        Author author = new Author();
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setEmail(email);
        return author;
    }

    private static Book createBook(String title, String isbn, String description, Integer publishedYear) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription(description);
        book.setPublishedYear(publishedYear);
        return book;
    }

    private static Review createReview(Integer rating, String comment, String reviewerName) {
        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setReviewerName(reviewerName);
        return review;
    }
}