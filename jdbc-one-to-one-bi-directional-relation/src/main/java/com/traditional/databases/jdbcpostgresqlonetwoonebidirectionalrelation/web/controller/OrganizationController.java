package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.controller;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.service.OrganizationService;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.request.OrganizationRequest;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.response.OrganizationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping("/organization/create")
    public Mono<ResponseEntity<OrganizationResponse>> createOrganization(@RequestBody Mono<OrganizationRequest> organisationRequestMono) {
        return organizationService.createOrganization(organisationRequestMono)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/organization/get/all")
    public Flux<OrganizationResponse> getOrganizations() {
        return organizationService.getAllOrganizations();
    }

    @GetMapping("/organization/get/{organizationId}")
    public Mono<ResponseEntity<OrganizationResponse>> getOrganization(@PathVariable(name = "organizationId") Long organizationId) {
        return organizationService.getOrganizationById(organizationId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @PutMapping("/organization/update/{organizationId}")
    public Mono<ResponseEntity<OrganizationResponse>> updateOrganization(@PathVariable(name = "organizationId") Long organizationId,
                                                     @RequestBody OrganizationRequest organizationRequest) {
        return organizationService.updateOrganization(organizationId, organizationRequest)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/organization/delete/{organizationId}")
    public Mono<ResponseEntity<OrganizationResponse>> deleteOrganization(@PathVariable(name = "organizationId") Long organizationId) {
        return organizationService.deleteOrganizationById(organizationId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}
