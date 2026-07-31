package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse  implements Serializable {

    private static final long serialVersionUID = 380022879284567L;

    private Long id;
    private String name;
    private String orgId;
    private AddressResponse address;
}
