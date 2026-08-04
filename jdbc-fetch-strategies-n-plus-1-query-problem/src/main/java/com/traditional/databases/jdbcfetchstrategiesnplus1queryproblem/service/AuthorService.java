package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository.AuthorRepository;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.mapper.AuthorMapper;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.AuthorRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.AuthorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<AuthorResponse> createAuthor(AuthorRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateRequest(request);
                    authorRepository.findByEmail(request.getEmail().trim())
                            .ifPresent(existing -> {
                                throw new IllegalArgumentException("Author already exists with email: " + request.getEmail().trim());
                            });

                    Author author = new Author();
                    author.setFirstName(request.getFirstName().trim());
                    author.setLastName(request.getLastName().trim());
                    author.setEmail(request.getEmail().trim());
                    Author saved = authorRepository.save(author);
                    return authorMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AuthorResponse> updateAuthor(Long authorId, AuthorRequest request) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    validateRequest(request);
                    Author author = findAuthorByIdWithBooks(authorId);
                    String email = request.getEmail().trim();
                    authorRepository.findByEmail(email)
                            .filter(existing -> !existing.getId().equals(author.getId()))
                            .ifPresent(existing -> {
                                throw new IllegalArgumentException("Author already exists with email: " + email);
                            });

                    author.setFirstName(request.getFirstName().trim());
                    author.setLastName(request.getLastName().trim());
                    author.setEmail(email);
                    Author saved = authorRepository.save(author);
                    return authorMapper.toResponse(saved);
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AuthorResponse> getAuthorById(Long authorId) {
        return Mono.fromCallable(() -> authorMapper.toResponse(findAuthorByIdWithBooks(authorId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<AuthorResponse> getAllAuthors() {
        return Mono.fromCallable(authorRepository::findAllWithBooks)
                .flatMapMany(Flux::fromIterable)
                .map(authorMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<AuthorResponse> deleteAuthor(Long authorId) {
        return Mono.fromCallable(() -> inTransaction(() -> {
                    Author author = findAuthorByIdWithBooks(authorId);
                    AuthorResponse response = authorMapper.toResponse(author);
                    authorRepository.delete(author);
                    return response;
                }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Author findAuthorByIdWithBooks(Long authorId) {
        return authorRepository.findByIdWithBooks(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with Id: " + authorId));
    }

    private void validateRequest(AuthorRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        requireText(request.getFirstName(), "firstName", 80);
        requireText(request.getLastName(), "lastName", 80);
        requireText(request.getEmail(), "email", 128);
        if (!request.getEmail().contains("@")) {
            throw new IllegalArgumentException("email must contain @");
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

