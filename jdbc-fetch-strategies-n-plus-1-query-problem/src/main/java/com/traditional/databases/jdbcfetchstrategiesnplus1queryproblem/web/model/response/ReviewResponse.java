package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Integer rating;
    private String comment;
    private String reviewerName;
    private BookSummaryResponse book;
}

