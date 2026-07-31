package com.traditional.databases.jdbcpostgresqlentityrelations.web.controller;

import com.traditional.databases.jdbcpostgresqlentityrelations.service.BookService;
import com.traditional.databases.jdbcpostgresqlentityrelations.web.model.BookRequest;
import com.traditional.databases.jdbcpostgresqlentityrelations.web.model.BookResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookRestController {

    private final BookService bookService;

    @PostMapping("/book/create")
    public Mono<ResponseEntity<BookResponse>> createBook(@RequestBody Mono<BookRequest> bookRequestMono) {
        return bookService.createNewBook(bookRequestMono)
                .map(book -> new ResponseEntity<>(book, HttpStatus.CREATED))
                .doOnNext(book -> log.info("created new book with id: {}", book.getBody() != null ? book.getBody().getId() : null));

    }
}
