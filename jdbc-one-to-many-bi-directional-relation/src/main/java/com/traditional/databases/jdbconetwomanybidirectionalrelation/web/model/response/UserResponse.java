package com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private Long roleId;
}
