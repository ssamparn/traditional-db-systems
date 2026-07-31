package com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {
    private String employeeCode;
    private String fullName;
    private WorkstationRequest workstation;
}

