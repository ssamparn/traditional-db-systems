package com.traditional.databases.jdbcpostgresqlentityrelations.mapper;

import com.traditional.databases.jdbcpostgresqlentityrelations.db.entity.Book;
import com.traditional.databases.jdbcpostgresqlentityrelations.web.model.BookRequest;
import org.springframework.stereotype.Service;

@Service
public class BookMapper {

    public Book toBookEntity(BookRequest request) {
        Book book = new Book();
        book.setTitle(request.getName());
        book.setIsbn(request.getIsbn());
        book.setTotalPages(request.getTotalPages());
        book.setRating(request.getRating());
        book.setPublishedDate(request.getPublishedDate());
        return book;
    }

}
