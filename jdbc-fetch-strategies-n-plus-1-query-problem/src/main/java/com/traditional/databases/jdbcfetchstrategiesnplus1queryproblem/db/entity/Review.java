package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private Integer rating;

    @Setter
    @Column(nullable = false, length = 1200)
    private String comment;

    @Setter
    @Column(name = "reviewer_name", nullable = false, length = 100)
    private String reviewerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id_fk", nullable = false)
    private Book book;

    public void assignBook(Book book) {
        this.book = book;
    }
}

