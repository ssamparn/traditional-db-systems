package com.traditional.databases.jdbconetoonebidirectionalrelation.mapper;

import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Address;
import com.traditional.databases.jdbconetoonebidirectionalrelation.web.model.response.AddressResponse;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }
        AddressResponse response = new AddressResponse();
        response.setBuilding(address.getBuilding());
        response.setCity(address.getCity());
        response.setCountry(address.getCountry());
        response.setState(address.getState());
        response.setStreet(address.getStreet());
        response.setZipcode(address.getZipcode());
        return response;
    }
}
