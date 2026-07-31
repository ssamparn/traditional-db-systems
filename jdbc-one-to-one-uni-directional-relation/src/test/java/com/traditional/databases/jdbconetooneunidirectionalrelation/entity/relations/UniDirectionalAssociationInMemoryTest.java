package com.traditional.databases.jdbconetooneunidirectionalrelation.entity.relations;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Employee;
import com.traditional.databases.jdbconetooneunidirectionalrelation.db.entity.Workstation;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class UniDirectionalAssociationInMemoryTest {

    @Test
    void ownerSideReplacement_shouldBeDeterministicBeforePersistence() {
        Employee employee = new Employee();
        employee.setEmployeeCode("EMP-7001");
        employee.setFullName("In Memory");

        Workstation oldWorkstation = createWorkstation("D-30", "HQ-Core", 7, "Platform");
        Workstation newWorkstation = createWorkstation("D-31", "HQ-Core", 8, "Platform");

        employee.setWorkstation(oldWorkstation);
        employee.setWorkstation(newWorkstation);

        assertThat(employee.getWorkstation()).isSameAs(newWorkstation);
    }

    @Test
    void workstationEntity_shouldNotContainEmployeeReference() {
        Field[] fields = Workstation.class.getDeclaredFields();

        boolean hasEmployeeReference = Arrays.stream(fields)
                .anyMatch(field -> field.getType().equals(Employee.class));

        assertThat(hasEmployeeReference).isFalse();
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

