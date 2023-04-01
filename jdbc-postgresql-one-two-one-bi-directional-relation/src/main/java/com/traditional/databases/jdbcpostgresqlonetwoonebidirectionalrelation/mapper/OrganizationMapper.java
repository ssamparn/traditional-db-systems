package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.mapper;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Address;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Organization;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.request.AddressRequest;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.request.OrganizationRequest;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.response.OrganizationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationMapper {

    private final AddressMapper addressMapper;

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

    public OrganizationResponse toResponse(Organization organization) {
        OrganizationResponse response = new OrganizationResponse();
        response.setId(organization.getId());
        response.setName(organization.getName());
        response.setOrgId(organization.getOrgId());
        response.setAddress(addressMapper.toAddressResponse(organization.getAddress()));
        return response;
    }

    public Organization updateEntity(Long organizationId, Organization organization, OrganizationRequest request) {
        organization.setId(organizationId);
        organization.setName(request.getOrganizationName());
        organization.setOrgId(request.getOrganizationId());
        Address address = createAddress(request.getAddress());
        organization.setAddress(address);
        return organization;
    }
}
