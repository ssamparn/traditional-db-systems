package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.controller;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Address;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;

    @GetMapping("/address/get/all")
    public List<Address> getAddress() {
        return addressRepository.findAll();
    }

}
