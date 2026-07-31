package com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.web.model.request.AddressRequest;
import com.traditional.databases.jdbcpostgresqlonetoonebidirectionalrelation.web.model.request.OrganizationRequest;

final class OrganizationRequestValidator {

    private OrganizationRequestValidator() {
    }

    static void validate(OrganizationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getOrganizationName(), "organizationName", 120);
        requireText(request.getOrganizationId(), "organizationId", 64);

        AddressRequest address = request.getAddress();
        if (address == null) {
            throw new IllegalArgumentException("address is required");
        }

        requireText(address.getBuilding(), "address.building", 64);
        requireText(address.getStreet(), "address.street", 128);
        requireText(address.getCity(), "address.city", 64);
        requireText(address.getState(), "address.state", 64);
        requireText(address.getCountry(), "address.country", 64);
        requireText(address.getZipcode(), "address.zipcode", 16);
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

