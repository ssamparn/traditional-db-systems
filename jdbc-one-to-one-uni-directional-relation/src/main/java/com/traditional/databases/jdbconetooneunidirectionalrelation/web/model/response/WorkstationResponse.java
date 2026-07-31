package com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkstationResponse implements Serializable {

    private static final long serialVersionUID = 2465871029856671L;

    private Long id;
    private String deskCode;
    private String building;
    private Integer floorNumber;
    private String zone;
}

