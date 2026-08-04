package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.db.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Setter
    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Setter
    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        if (book == null || books.contains(book)) {
            return;
        }
        books.add(book);
        book.assignAuthor(this);
    }

    public void removeBook(Book book) {
        if (book == null) {
            return;
        }
        if (books.remove(book)) {
            book.assignAuthor(null);
        }
    }
}

