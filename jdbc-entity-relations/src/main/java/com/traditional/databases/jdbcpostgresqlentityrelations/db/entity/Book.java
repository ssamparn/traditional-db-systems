package com.traditional.databases.jdbcpostgresqlentityrelations.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@Entity
@Table(name = "books")
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "isbn")
    private Long isbn;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "published_date")
    private Date publishedDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "photo_id", unique = true)
    private Photo photo;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "books_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Collection<Category> categories = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "books_authors",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Collection<Author> authors = new ArrayList<>();

    public void setPhoto(Photo photo) {
        this.photo = photo;
        if (photo != null && photo.getBook() != this) {
            photo.setBook(this);
        }
    }

    public void addCategory(Category category) {
        if (category == null || categories.contains(category)) {
            return;
        }
        categories.add(category);
        category.getBooks().add(this);
    }

    public void addAuthor(Author author) {
        if (author == null || authors.contains(author)) {
            return;
        }
        authors.add(author);
        author.getBooks().add(this);
    }
}
