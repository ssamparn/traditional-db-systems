package com.traditional.databases.jdbconetooneunidirectionalrelation.entity.orphanhandling;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Employee;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Workstation;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository.EmployeeRepository;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository.WorkstationRepository;
import com.traditional.databases.jdbconetooneunidirectionalrelation.service.EmployeeService;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.EmployeeRequest;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.request.WorkstationRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EmployeeOrphanHandlingIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkstationRepository workstationRepository;

    @Autowired
    private EmployeeService employeeService;

    @AfterEach
    void cleanup() {
        employeeRepository.deleteAll();
        workstationRepository.deleteAll();
    }

    @Test
    void updateEmployee_shouldRemoveReplacedWorkstationAsOrphan() {
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-3001");
        employee.setFullName("Orphan Candidate");

        Workstation initialWorkstation = new Workstation();
        initialWorkstation.setDeskCode("D-1");
        initialWorkstation.setBuilding("HQ-North");
        initialWorkstation.setFloorNumber(2);
        initialWorkstation.setZone("Ops");

        employee.setWorkstation(initialWorkstation);

        Employee savedEmployee = employeeRepository.save(employee);
        Long oldWorkstationId = savedEmployee.getWorkstation().getId();

        EmployeeRequest updateRequest = new EmployeeRequest(
                "EMP-3001",
                "Orphan Candidate Updated",
                new WorkstationRequest("D-9", "HQ-South", 5, "Platform")
        );

        employeeService.updateEmployee(savedEmployee.getId(), updateRequest).block();

        Employee updatedEmployee = employeeRepository.findById(savedEmployee.getId()).orElseThrow();
        Long newWorkstationId = updatedEmployee.getWorkstation().getId();

        assertThat(newWorkstationId).isNotEqualTo(oldWorkstationId);
        assertThat(workstationRepository.existsById(oldWorkstationId)).isFalse();
        assertThat(updatedEmployee.getWorkstation().getBuilding()).isEqualTo("HQ-South");
    }

    @Test
    void deleteEmployee_shouldCascadeDeleteWorkstation() {
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-3002");
        employee.setFullName("Cascade Delete");

        Workstation workstation = new Workstation();
        workstation.setDeskCode("D-88");
        workstation.setBuilding("HQ-West");
        workstation.setFloorNumber(11);
        workstation.setZone("Security");

        employee.setWorkstation(workstation);

        Employee saved = employeeRepository.save(employee);
        Long workstationId = saved.getWorkstation().getId();

        employeeService.deleteEmployeeById(saved.getId()).block();

        assertThat(employeeRepository.existsById(saved.getId())).isFalse();
        assertThat(workstationRepository.existsById(workstationId)).isFalse();
    }
}

