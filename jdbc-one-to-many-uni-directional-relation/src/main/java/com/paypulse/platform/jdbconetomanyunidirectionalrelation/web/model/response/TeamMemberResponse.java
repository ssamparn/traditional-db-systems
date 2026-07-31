package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TeamMemberResponse {
    private Long teamId;
    private String teamCode;
    private Long memberId;
    private String memberFirstName;
    private String memberLastName;
    private String memberEmail;

    public TeamMemberResponse(Long teamId,
                              String teamCode,
                              Long memberId,
                              String memberFirstName,
                              String memberLastName,
                              String memberEmail) {
        this.teamId = teamId;
        this.teamCode = teamCode;
        this.memberId = memberId;
        this.memberFirstName = memberFirstName;
        this.memberLastName = memberLastName;
        this.memberEmail = memberEmail;
    }
}

