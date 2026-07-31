package com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private String name;
    private String description;
    private List<UserResponse> users;
}
