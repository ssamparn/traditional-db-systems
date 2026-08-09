package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchStrategyReportResponse {

    private String scenario;
    private long queryCount;
    private long loadedAssociationCount;
    private int authorCount;
    private int bookCount;
    private long reviewCount;
    private String notes;
    private List<FetchAuthorGraphResponse> authors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FetchAuthorGraphResponse {

        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private List<FetchBookGraphResponse> books;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FetchBookGraphResponse {

        private Long id;
        private String title;
        private String isbn;
        private Integer publishedYear;
        private List<ReviewSummaryResponse> reviews;
    }
}

