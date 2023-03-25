package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.controller;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.repository.AddressRepository;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.mapper.AddressMapper;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.response.AddressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AddressController {

    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;

    @GetMapping("/address/get/all")
    public Flux<AddressResponse> getAddress() {
        return Flux.fromIterable(this.addressRepository.findAll())
                .map(addressMapper::toAddressResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
