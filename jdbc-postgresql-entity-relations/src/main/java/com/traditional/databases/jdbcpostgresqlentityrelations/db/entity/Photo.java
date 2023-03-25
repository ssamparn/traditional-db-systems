package com.traditional.databases.jdbcpostgresqlentityrelations.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "photo")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long id;

    @Column(name = "small_url")
    private String urlSmall;

    @Column(name = "medium_url")
    private String urlMedium;

    @Column(name = "large_url")
    private String urlLarge;

    // bi-directional
    @OneToOne(mappedBy = "photo")
    private Book book;
}