package com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkstationRequest {
    private String deskCode;
    private String building;
    private Integer floorNumber;
    private String zone;
}

