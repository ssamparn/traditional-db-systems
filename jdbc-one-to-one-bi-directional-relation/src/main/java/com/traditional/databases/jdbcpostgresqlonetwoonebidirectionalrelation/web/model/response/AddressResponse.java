package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse implements Serializable {

    private static final long serialVersionUID = 926840284567L;

    private String building;
    private String street;
    private String city;
    private String state;
    private String country;
    private String zipcode;
}
