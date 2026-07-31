package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private Long id;
    private String teamCode;
    private String name;
    private String description;
    private List<MemberResponse> members;
}

