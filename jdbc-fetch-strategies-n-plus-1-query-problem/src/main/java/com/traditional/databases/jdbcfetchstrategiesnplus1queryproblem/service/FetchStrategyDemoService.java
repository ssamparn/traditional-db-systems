package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.BookRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.ReviewRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper.AuthorMapper;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.AuthorResponse;
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
                            "1 + N + N pattern can happen while mapping books and reviews lazily"
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> oneParentJoinFetch(Long authorId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    Author author = authorRepository.findByIdWithBooksGraph(authorId)
                            .orElseThrow(() -> new ResourceNotFoundException("Author not found with Id: " + authorId));
                    return buildReport(
                            "ONE_PARENT_JOIN_FETCH",
                            List.of(author),
                            statistics,
                            reviewRepository.countByAuthorId(authorId),
                            "Optimized fetch plan preloads the parent and books in fewer SQL statements"
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
                            "EntityGraph keeps repository API clean while forcing the parent->books fetch plan"
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
                            "Classic N+1+N query explosion when traversing multiple parent graphs"
                    );
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<FetchStrategyReportResponse> manyParentsJoinFetch() {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Statistics statistics = resetStatistics();
                    List<Author> authors = authorRepository.findAllWithBooksGraph();
                    return buildReport(
                            "MANY_PARENTS_JOIN_FETCH",
                            authors,
                            statistics,
                            reviewRepository.countByAuthorIds(authors.stream().map(Author::getId).toList()),
                            "Optimized fetch plan reduces query fan-out for list retrieval while leaving reviews lazy"
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
                            "EntityGraph alternative to JPQL join fetch for read models while leaving reviews lazy"
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
                                                    String note) {
        List<AuthorResponse> responses = authors.stream().map(authorMapper::toResponse).toList();
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

