package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private Long teamId;
}

