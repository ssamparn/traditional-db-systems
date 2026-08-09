package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchBookGraphResponse {

    private Long id;
    private String title;
    private String isbn;
    private Integer publishedYear;
    private List<ReviewSummaryResponse> reviews;
}

