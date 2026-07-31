package com.traditional.databases.jdbcpostgresqlentityrelations.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
public class BookRequest {
    private String name;
    private Long isbn;
    private Integer totalPages;
    private Double rating;
    private Date publishedDate;
}
