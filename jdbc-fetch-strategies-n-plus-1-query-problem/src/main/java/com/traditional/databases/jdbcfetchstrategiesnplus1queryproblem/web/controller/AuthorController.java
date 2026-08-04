package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.controller;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service.AuthorService;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.AuthorRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.AuthorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping("/author/create")
    public Mono<ResponseEntity<AuthorResponse>> createAuthor(@RequestBody AuthorRequest request) {
        return authorService.createAuthor(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/author/get/{authorId}")
    public Mono<ResponseEntity<AuthorResponse>> getAuthor(@PathVariable Long authorId) {
        return authorService.getAuthorById(authorId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/author/get/all")
    public Flux<AuthorResponse> getAuthors() {
        return authorService.getAllAuthors();
    }

    @PutMapping("/author/update/{authorId}")
    public Mono<ResponseEntity<AuthorResponse>> updateAuthor(@PathVariable Long authorId,
                                                             @RequestBody AuthorRequest request) {
        return authorService.updateAuthor(authorId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/author/delete/{authorId}")
    public Mono<ResponseEntity<AuthorResponse>> deleteAuthor(@PathVariable Long authorId) {
        return authorService.deleteAuthor(authorId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

