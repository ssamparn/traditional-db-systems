package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private String building;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipcode;
}
