package com.traditional.databases.jdbcpostgresqlentityrelations.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class BookResponse {
    private Long id;
    private String title;
    private Long isbn;
    private Integer totalPages;
    private Double rating;
    private Date publishedDate;
}

