package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.mapper;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Address;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Organization;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.AddressRequest;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.OrganizationRequest;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMapper {

    public Organization toEntity(final OrganizationRequest request) {
        Organization entity = new Organization();
        entity.setName(request.getOrganizationName());
        entity.setOrgId(request.getOrganizationId());
        Address address = createAddress(request.getAddress());
        entity.setAddress(address);
        return entity;
    }

    private Address createAddress(AddressRequest request) {
        Address entity = new Address();
        entity.setBuilding(request.getBuilding());
        entity.setStreet(request.getStreet());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setCountry(request.getCountry());
        entity.setZipcode(request.getZipcode());
        return entity;
    }
}
