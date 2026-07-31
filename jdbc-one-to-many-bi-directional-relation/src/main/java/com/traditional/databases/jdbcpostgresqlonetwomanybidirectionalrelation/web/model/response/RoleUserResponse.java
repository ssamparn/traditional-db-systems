package com.traditional.databases.jdbcpostgresqlonetwomanybidirectionalrelation.web.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoleUserResponse {
    private Long roleId;
    private String roleName;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;

    public RoleUserResponse(Long roleId, String roleName, Long userId, String userFirstName, String userLastName, String userEmail) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.userId = userId;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.userEmail = userEmail;
    }
}
