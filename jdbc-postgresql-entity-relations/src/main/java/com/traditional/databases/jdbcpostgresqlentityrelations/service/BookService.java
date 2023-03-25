package com.traditional.databases.jdbcpostgresqlentityrelations.service;

import com.traditional.databases.jdbcpostgresqlentityrelations.db.entity.Book;
import com.traditional.databases.jdbcpostgresqlentityrelations.db.repository.BookRepository;
import com.traditional.databases.jdbcpostgresqlentityrelations.mapper.BookMapper;
import com.traditional.databases.jdbcpostgresqlentityrelations.web.model.BookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookMapper bookMapper;
    private final BookRepository bookRepository;

    public Mono<Book> createNewBook(Mono<BookRequest> bookRequestMono) {
        log.debug("creating new book: {}", bookRequestMono);

        return bookRequestMono
                .map(bookMapper::toBookEntity)
                .map(bookRepository::save)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
