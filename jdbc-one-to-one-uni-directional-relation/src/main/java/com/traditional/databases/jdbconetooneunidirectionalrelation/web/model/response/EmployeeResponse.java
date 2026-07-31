package com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse implements Serializable {

    private static final long serialVersionUID = 7213699018335679L;

    private Long id;
    private String employeeCode;
    private String fullName;
    private WorkstationResponse workstation;
}

