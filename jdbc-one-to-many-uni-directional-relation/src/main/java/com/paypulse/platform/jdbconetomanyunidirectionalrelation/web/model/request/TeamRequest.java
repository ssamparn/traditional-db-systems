package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {
    private String teamCode;
    private String name;
    private String description;
    private List<MemberRequest> members;
}

