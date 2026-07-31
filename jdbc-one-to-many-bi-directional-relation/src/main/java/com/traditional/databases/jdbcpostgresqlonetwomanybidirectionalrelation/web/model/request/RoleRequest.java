package com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    private String name;
    private String description;
    private List<UserRequest> users;
}
