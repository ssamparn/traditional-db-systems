package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, length = 160)
    private String title;

    @Setter
    @Column(nullable = false, unique = true, length = 32)
    private String isbn;

    @Setter
    @Column(length = 600)
    private String description;

    @Setter
    @Column(name = "published_year")
    private Integer publishedYear;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id_fk", nullable = false)
    private Author author;

    @OrderBy("id ASC")
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Review> reviews = new LinkedHashSet<>();

    public void assignAuthor(Author author) {
        this.author = author;
    }

    public void addReview(Review review) {
        if (review == null || reviews.contains(review)) {
            return;
        }
        reviews.add(review);
        review.assignBook(this);
    }

    public void removeReview(Review review) {
        if (review == null) {
            return;
        }
        if (reviews.remove(review)) {
            review.assignBook(null);
        }
    }
}

