package com.traditional.databases.jdbconetooneunidirectionalrelation.mapper;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Employee;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Workstation;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.EmployeeRequest;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.response.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {

    private final WorkstationMapper workstationMapper;

    public Employee toEntity(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFullName(request.getFullName());
        Workstation workstation = workstationMapper.toEntity(request.getWorkstation());
        employee.setWorkstation(workstation);
        return employee;
    }

    public Employee updateEntity(Long employeeId, Employee employee, EmployeeRequest request) {
        employee.setId(employeeId);
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFullName(request.getFullName());
        Workstation workstation = workstationMapper.toEntity(request.getWorkstation());
        employee.setWorkstation(workstation);
        return employee;
    }

    public EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setEmployeeCode(employee.getEmployeeCode());
        response.setFullName(employee.getFullName());
        response.setWorkstation(workstationMapper.toResponse(employee.getWorkstation()));
        return response;
    }
}

