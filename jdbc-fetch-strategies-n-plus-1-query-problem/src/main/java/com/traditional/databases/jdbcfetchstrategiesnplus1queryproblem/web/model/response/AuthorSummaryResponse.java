package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorSummaryResponse {

    private Long id;
    private String fullName;
    private String email;
}

