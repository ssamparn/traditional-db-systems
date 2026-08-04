package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    private String title;
    private String isbn;
    private String description;
    private Integer publishedYear;
    private Long authorId;
}

