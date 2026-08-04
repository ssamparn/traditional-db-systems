package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.controller;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service.BookService;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request.BookRequest;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.BookResponse;
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
public class BookController {

    private final BookService bookService;

    @PostMapping("/book/create")
    public Mono<ResponseEntity<BookResponse>> createBook(@RequestBody BookRequest request) {
        return bookService.createBook(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/book/get/{bookId}")
    public Mono<ResponseEntity<BookResponse>> getBook(@PathVariable Long bookId) {
        return bookService.getBookById(bookId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/book/get/all")
    public Flux<BookResponse> getBooks() {
        return bookService.getAllBooks();
    }

    @PutMapping("/book/update/{bookId}")
    public Mono<ResponseEntity<BookResponse>> updateBook(@PathVariable Long bookId,
                                                         @RequestBody BookRequest request) {
        return bookService.updateBook(bookId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/book/delete/{bookId}")
    public Mono<ResponseEntity<BookResponse>> deleteBook(@PathVariable Long bookId) {
        return bookService.deleteBook(bookId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

