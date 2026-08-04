package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.repository;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"book", "book.author"})
    @Query("select r from Review r where r.id = :reviewId")
    Optional<Review> findByIdWithBookAndAuthor(@Param("reviewId") Long reviewId);

    @EntityGraph(attributePaths = {"book", "book.author"})
    @Query("select r from Review r where r.book.id = :bookId order by r.id")
    List<Review> findAllByBookId(@Param("bookId") Long bookId);

    @Query("select count(r) from Review r where r.book.author.id = :authorId")
    long countByAuthorId(@Param("authorId") Long authorId);

    @Query("select count(r) from Review r where r.book.author.id in :authorIds")
    long countByAuthorIds(@Param("authorIds") List<Long> authorIds);
}

