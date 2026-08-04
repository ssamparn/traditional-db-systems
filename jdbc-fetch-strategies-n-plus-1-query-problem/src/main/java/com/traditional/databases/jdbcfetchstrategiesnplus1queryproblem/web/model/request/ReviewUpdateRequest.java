package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateRequest {

    private Integer rating;
    private String comment;
    private String reviewerName;
}

