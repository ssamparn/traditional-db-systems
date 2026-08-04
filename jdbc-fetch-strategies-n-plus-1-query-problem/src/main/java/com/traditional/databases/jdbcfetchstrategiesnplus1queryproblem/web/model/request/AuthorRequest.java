package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorRequest {

    private String firstName;
    private String lastName;
    private String email;
}

