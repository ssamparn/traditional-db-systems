package com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}

