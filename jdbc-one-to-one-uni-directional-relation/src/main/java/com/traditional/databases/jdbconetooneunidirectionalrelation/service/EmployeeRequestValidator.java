package com.traditional.databases.jdbconetooneunidirectionalrelation.service;

import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.EmployeeRequest;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.WorkstationRequest;

final class EmployeeRequestValidator {

    private EmployeeRequestValidator() {
    }

    static void validate(EmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getEmployeeCode(), "employeeCode", 64);
        requireText(request.getFullName(), "fullName", 120);

        WorkstationRequest workstation = request.getWorkstation();
        if (workstation == null) {
            throw new IllegalArgumentException("workstation is required");
        }

        requireText(workstation.getDeskCode(), "workstation.deskCode", 32);
        requireText(workstation.getBuilding(), "workstation.building", 64);
        requireText(workstation.getZone(), "workstation.zone", 32);

        Integer floorNumber = workstation.getFloorNumber();
        if (floorNumber == null) {
            throw new IllegalArgumentException("workstation.floorNumber is required");
        }
        if (floorNumber < 0 || floorNumber > 200) {
            throw new IllegalArgumentException("workstation.floorNumber must be between 0 and 200");
        }
    }

    private static void requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxLength + " characters");
        }
    }
}

