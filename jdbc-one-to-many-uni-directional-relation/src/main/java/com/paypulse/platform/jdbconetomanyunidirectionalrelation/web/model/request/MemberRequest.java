package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
}

