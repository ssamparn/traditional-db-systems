package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper.AuthorMapper;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.FetchStrategyReportResponse.FetchAuthorGraphResponse;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.FetchStrategyReportResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class FetchStrategyDemoService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final AuthorMapper authorMapper;
    private final EntityManagerFactory entityManagerFactory;
    private final TransactionTemplate transactionTemplate;

    public Mono<FetchStrategyReportResponse> oneParentLazy(Long authorId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    Author author = authorRepository.findById(authorId)
                            .orElseThrow(() -> new ResourceNotFoundException("Author not found with Id: " + authorId));
                    return buildReport(
                            "ONE_PARENT_LAZY",
                            List.of(author),
                            statistics,
                            authorMapper.countReviews(author),
                            "1 + N + N pattern can happen while mapping books and reviews lazily",
                            true
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> oneParentJoinFetch(Long authorId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    Author author = authorRepository.findByIdWithBooks(authorId)
                            .orElseThrow(() -> new ResourceNotFoundException("Author not found with Id: " + authorId));
                    return buildReport(
                            "ONE_PARENT_JOIN_FETCH",
                            List.of(author),
                            statistics,
                            reviewRepository.countByAuthorId(authorId),
                            "JPQL join fetch preloads Author -> books in fewer SQL statements while keeping reviews lazy for contrast",
                            false
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> oneParentEntityGraph(Long authorId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    Author author = authorRepository.findByIdWithBooksGraph(authorId)
                            .orElseThrow(() -> new ResourceNotFoundException("Author not found with Id: " + authorId));
                    return buildReport(
                            "ONE_PARENT_ENTITY_GRAPH",
                            List.of(author),
                            statistics,
                            reviewRepository.countByAuthorId(authorId),
                            "EntityGraph keeps repository API clean while forcing the Author -> books fetch plan and leaving reviews lazy",
                            false
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> oneParentJoinFetchDeep(Long authorId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    Author author = authorRepository.findByIdWithBooksAndReviewsJoinFetch(authorId)
                            .orElseThrow(() -> new ResourceNotFoundException("Author not found with Id: " + authorId));
                    return buildReport(
                            "ONE_PARENT_JOIN_FETCH_DEEP",
                            List.of(author),
                            statistics,
                            authorMapper.countReviews(author),
                            "JPQL join fetch preloads Author -> books -> reviews to collapse the N+1+N chain for one aggregate",
                            true
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> oneParentEntityGraphDeep(Long authorId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    Author author = authorRepository.findByIdWithBooksAndReviewsEntityGraph(authorId)
                            .orElseThrow(() -> new ResourceNotFoundException("Author not found with Id: " + authorId));
                    return buildReport(
                            "ONE_PARENT_ENTITY_GRAPH_DEEP",
                            List.of(author),
                            statistics,
                            authorMapper.countReviews(author),
                            "EntityGraph preloads Author -> books -> reviews without embedding the fetch plan in JPQL",
                            true
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> manyParentsLazy() {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    List<Author> authors = authorRepository.findAllByOrderByIdAsc();
                    return buildReport(
                            "MANY_PARENTS_LAZY",
                            authors,
                            statistics,
                            authors.stream().mapToInt(authorMapper::countReviews).sum(),
                            "Classic N+1+N query explosion when traversing multiple parent graphs",
                            true
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> manyParentsJoinFetch() {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    List<Author> authors = authorRepository.findAllWithBooks();
                    return buildReport(
                            "MANY_PARENTS_JOIN_FETCH",
                            authors,
                            statistics,
                            reviewRepository.countByAuthorIds(authors.stream().map(Author::getId).toList()),
                            "JPQL join fetch reduces query fan-out for Author -> books list retrieval while leaving reviews lazy",
                            false
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> manyParentsEntityGraph() {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    List<Author> authors = authorRepository.findAllWithBooksGraph();
                    return buildReport(
                            "MANY_PARENTS_ENTITY_GRAPH",
                            authors,
                            statistics,
                            reviewRepository.countByAuthorIds(authors.stream().map(Author::getId).toList()),
                            "EntityGraph alternative to JPQL join fetch for Author -> books list retrieval while leaving reviews lazy",
                            false
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> manyParentsJoinFetchDeep() {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    List<Author> authors = authorRepository.findAllWithBooksAndReviewsJoinFetch();
                    return buildReport(
                            "MANY_PARENTS_JOIN_FETCH_DEEP",
                            authors,
                            statistics,
                            authors.stream().mapToInt(authorMapper::countReviews).sum(),
                            "JPQL join fetch preloads Author -> books -> reviews for the full list, avoiding the N+1+N chain at the cost of wider joined rows",
                            true
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> manyParentsEntityGraphDeep() {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    List<Author> authors = authorRepository.findAllWithBooksAndReviewsEntityGraph();
                    return buildReport(
                            "MANY_PARENTS_ENTITY_GRAPH_DEEP",
                            authors,
                            statistics,
                            authors.stream().mapToInt(authorMapper::countReviews).sum(),
                            "EntityGraph preloads Author -> books -> reviews for the full list while keeping repository method names cleaner than custom JPQL",
                            true
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> manyBooksWithEagerAuthor() {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    List<Book> books = bookRepository.findAllByOrderByIdAsc();
                    long loadedAssociations = books.stream().map(Book::getAuthor).filter(Objects::nonNull).count();

                    return new FetchStrategyReportResponse(
                            "MANY_BOOKS_EAGER_MANY_TO_ONE",
                            statistics.getPrepareStatementCount(),
                            loadedAssociations,
                            0,
                            books.size(),
                            0,
                            "FetchType.EAGER on many-to-one loads authors even if caller only needs book fields",
                            List.of()
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private FetchStrategyReportResponse buildReport(String scenario,
                                                    List<Author> authors,
                                                    Statistics statistics,
                                                    long reviewCount,
                                                    String note,
                                                    boolean includeReviewsInPayload) {
        List<FetchAuthorGraphResponse> responses = includeReviewsInPayload
                ? authors.stream().map(authorMapper::toFetchGraphWithReviews).toList()
                : authors.stream().map(authorMapper::toFetchGraph).toList();
        int bookCount = authors.stream().mapToInt(authorMapper::countBooks).sum();
        long loadedAssociations = authorMapper.countLoadedAuthorsOnBooks(authors);

        return new FetchStrategyReportResponse(
                scenario,
                statistics.getPrepareStatementCount(),
                loadedAssociations,
                authors.size(),
                bookCount,
                reviewCount,
                note,
                responses
        );
    }

    private Statistics resetStatistics() {
        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        return statistics;
    }

    private <T> T inTransaction(Supplier<T> supplier) {
        T result = transactionTemplate.execute(status -> supplier.get());
        if (result == null) {
            throw new IllegalStateException("Transaction returned null result");
        }
        return result;
    }
}

