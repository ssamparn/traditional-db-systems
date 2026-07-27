package com.traditional.databases.jdbcpostgresqlmanytwomanybidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleSummaryResponse {
    private Long id;
    private String name;
    private String description;
}

