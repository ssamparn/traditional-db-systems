package com.traditional.databases.jdbconetooneunidirectionalrelation.entity.relations;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Employee;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Workstation;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository.EmployeeRepository;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository.WorkstationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UniDirectionalAssociationIntegrationTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private WorkstationRepository workstationRepository;

    @AfterEach
    void cleanup() {
        employeeRepository.deleteAll();
        workstationRepository.deleteAll();
    }

    @Test
    void settingOwnerSide_shouldPersistAssociationAfterPersist() {
        Employee employee = createEmployee("EMP-5001", "Owner Persist");
        Workstation workstation = createWorkstation("D-21", "HQ-East", 3, "Core");

        employee.setWorkstation(workstation);

        Employee saved = employeeRepository.saveAndFlush(employee);
        Employee reloaded = employeeRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getWorkstation()).isNotNull();
        assertThat(reloaded.getWorkstation().getId()).isNotNull();
        assertThat(workstationRepository.existsById(reloaded.getWorkstation().getId())).isTrue();
    }

    @Test
    void replacingOwnerSideAssociation_shouldReplaceAssociationBeforeFlushAndDeleteOldAssociationAfterPersist() {
        Employee employee = createEmployee("EMP-5002", "Replacement Persist");
        Workstation oldWorkstation = createWorkstation("D-22", "HQ-East", 4, "Core");
        Workstation newWorkstation = createWorkstation("D-23", "HQ-East", 5, "Core");

        employee.setWorkstation(oldWorkstation);
        Employee savedInitial = employeeRepository.saveAndFlush(employee);
        Long oldWorkstationId = savedInitial.getWorkstation().getId();

        savedInitial.setWorkstation(newWorkstation);
        Employee savedUpdated = employeeRepository.saveAndFlush(savedInitial);

        assertThat(savedUpdated.getWorkstation()).isNotNull();
        assertThat(savedUpdated.getWorkstation().getDeskCode()).isEqualTo("D-23");
        assertThat(workstationRepository.existsById(oldWorkstationId)).isFalse();
    }

    @Test
    void settingOwnerSideForTwoOwnersWithIndependentChildren_shouldPersistBothAssociationsAfterPersist() {
        Employee firstEmployee = createEmployee("EMP-5003", "First Owner Persist");
        Employee secondEmployee = createEmployee("EMP-5004", "Second Owner Persist");
        Workstation firstWorkstation = createWorkstation("D-24", "HQ-East", 6, "Core");
        Workstation secondWorkstation = createWorkstation("D-25", "HQ-West", 7, "Platform");

        firstEmployee.setWorkstation(firstWorkstation);
        secondEmployee.setWorkstation(secondWorkstation);

        Employee savedFirst = employeeRepository.saveAndFlush(firstEmployee);
        Employee savedSecond = employeeRepository.saveAndFlush(secondEmployee);

        Employee reloadedFirst = employeeRepository.findById(savedFirst.getId()).orElseThrow();
        Employee reloadedSecond = employeeRepository.findById(savedSecond.getId()).orElseThrow();

        assertThat(reloadedFirst.getWorkstation()).isNotNull();
        assertThat(reloadedSecond.getWorkstation()).isNotNull();
        assertThat(reloadedFirst.getWorkstation().getDeskCode()).isEqualTo("D-24");
        assertThat(reloadedSecond.getWorkstation().getDeskCode()).isEqualTo("D-25");
        assertThat(reloadedFirst.getWorkstation().getId()).isNotEqualTo(reloadedSecond.getWorkstation().getId());
    }

    @Test
    void reassigningWorkstationBetweenOwners_shouldUseReplacementWorkstationBeforeFlushAndPersistBothOwnersAfterPersist() {
        Employee firstEmployee = createEmployee("EMP-5005", "First Reassign Owner");
        Employee secondEmployee = createEmployee("EMP-5006", "Second Reassign Owner");
        Workstation originalWorkstation = createWorkstation("D-26", "HQ-North", 8, "Ops");
        Workstation replacementWorkstation = createWorkstation("D-27", "HQ-South", 9, "Ops");

        firstEmployee.setWorkstation(originalWorkstation);
        secondEmployee.setWorkstation(originalWorkstation);

        // Mandatory owner FK: previous owner must keep a valid replacement before flush.
        firstEmployee.setWorkstation(replacementWorkstation);

        Employee savedFirst = employeeRepository.saveAndFlush(firstEmployee);
        Employee savedSecond = employeeRepository.saveAndFlush(secondEmployee);

        Employee reloadedFirst = employeeRepository.findById(savedFirst.getId()).orElseThrow();
        Employee reloadedSecond = employeeRepository.findById(savedSecond.getId()).orElseThrow();

        assertThat(reloadedFirst.getWorkstation()).isNotNull();
        assertThat(reloadedSecond.getWorkstation()).isNotNull();
        assertThat(reloadedFirst.getWorkstation().getDeskCode()).isEqualTo("D-27");
        assertThat(reloadedSecond.getWorkstation().getDeskCode()).isEqualTo("D-26");
        assertThat(reloadedFirst.getWorkstation().getId()).isNotEqualTo(reloadedSecond.getWorkstation().getId());
    }

    private Employee createEmployee(String code, String name) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFullName(name);
        return employee;
    }

    private Workstation createWorkstation(String deskCode, String building, Integer floor, String zone) {
        Workstation workstation = new Workstation();
        workstation.setDeskCode(deskCode);
        workstation.setBuilding(building);
        workstation.setFloorNumber(floor);
        workstation.setZone(zone);
        return workstation;
    }
}
