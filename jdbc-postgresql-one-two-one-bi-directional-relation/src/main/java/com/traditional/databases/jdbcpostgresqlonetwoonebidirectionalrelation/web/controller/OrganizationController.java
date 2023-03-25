package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.controller;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Organization;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.service.OrganizationService;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.OrganizationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/organization/create")
    public Mono<ResponseEntity<Organization>> createOrganization(@RequestBody Mono<OrganizationRequest> organisationRequestMono) {
        return organizationService.createOrganization(organisationRequestMono)
                .map(organization -> new ResponseEntity<>(organization, HttpStatus.CREATED));
    }
}
