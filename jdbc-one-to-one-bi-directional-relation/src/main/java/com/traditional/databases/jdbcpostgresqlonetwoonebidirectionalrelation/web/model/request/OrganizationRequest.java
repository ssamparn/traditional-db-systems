package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {
    private String organizationName;
    private String organizationId;
    private AddressRequest address;
}
