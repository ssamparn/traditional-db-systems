package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<BookSummaryResponse> books;
}

