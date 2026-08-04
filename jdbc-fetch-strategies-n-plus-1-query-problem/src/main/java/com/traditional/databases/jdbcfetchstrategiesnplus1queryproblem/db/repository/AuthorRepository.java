package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Author;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByEmail(String email);

    @Query("select a from Author a left join fetch a.books where a.id = :authorId")
    Optional<Author> findByIdWithBooks(@Param("authorId") Long authorId);

    @EntityGraph(attributePaths = "books")
    @Query("select a from Author a where a.id = :authorId")
    Optional<Author> findByIdWithBooksGraph(@Param("authorId") Long authorId);

    @Query("select distinct a from Author a left join fetch a.books b left join fetch b.reviews where a.id = :authorId")
    Optional<Author> findByIdWithBooksAndReviewsJoinFetch(@Param("authorId") Long authorId);

    @EntityGraph(attributePaths = {"books", "books.reviews"})
    @Query("select a from Author a where a.id = :authorId")
    Optional<Author> findByIdWithBooksAndReviewsEntityGraph(@Param("authorId") Long authorId);

    @Query("select distinct a from Author a left join fetch a.books b left join fetch b.reviews order by a.id")
    List<Author> findAllWithBooksAndReviewsJoinFetch();

    @Query("select distinct a from Author a left join fetch a.books order by a.id")
    List<Author> findAllWithBooks();

    @EntityGraph(attributePaths = "books")
    @Query("select a from Author a order by a.id")
    List<Author> findAllWithBooksGraph();

    @EntityGraph(attributePaths = {"books", "books.reviews"})
    @Query("select a from Author a order by a.id")
    List<Author> findAllWithBooksAndReviewsEntityGraph();

    List<Author> findAllByOrderByIdAsc();
}

