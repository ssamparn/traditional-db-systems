package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    @EntityGraph(attributePaths = {"author", "reviews"})
    @Query("select b from Book b where b.id = :bookId")
    Optional<Book> findByIdWithAuthorAndReviews(@Param("bookId") Long bookId);

    @EntityGraph(attributePaths = {"author", "reviews"})
    @Query("select b from Book b order by b.id")
    List<Book> findAllWithAuthorAndReviews();

    List<Book> findAllByOrderByIdAsc();
}

