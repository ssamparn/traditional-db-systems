package com.traditional.databases.jdbconetooneunidirectionalrelation.mapper;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Workstation;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.WorkstationRequest;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.response.WorkstationResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkstationMapper {

    public Workstation toEntity(WorkstationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("workstation is required");
        }

        Workstation workstation = new Workstation();
        workstation.setDeskCode(request.getDeskCode());
        workstation.setBuilding(request.getBuilding());
        workstation.setFloorNumber(request.getFloorNumber());
        workstation.setZone(request.getZone());
        return workstation;
    }

    public WorkstationResponse toResponse(Workstation workstation) {
        if (workstation == null) {
            return null;
        }

        WorkstationResponse response = new WorkstationResponse();
        response.setId(workstation.getId());
        response.setDeskCode(workstation.getDeskCode());
        response.setBuilding(workstation.getBuilding());
        response.setFloorNumber(workstation.getFloorNumber());
        response.setZone(workstation.getZone());
        return response;
    }
}

