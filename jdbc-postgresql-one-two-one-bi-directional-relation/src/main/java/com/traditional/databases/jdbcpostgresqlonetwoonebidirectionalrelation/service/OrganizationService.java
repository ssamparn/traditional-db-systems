package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Organization;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.repository.AddressRepository;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.repository.OrganizationRepository;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.mapper.OrganizationMapper;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.request.OrganizationRequest;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.response.OrganizationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationMapper organizationMapper;
    private final OrganizationRepository organizationRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public Mono<OrganizationResponse> createOrganization(final Mono<OrganizationRequest> organizationMono) {
        return organizationMono
                .map(organizationMapper::toEntity)
                .map(organizationRepository::save)
                .map(organizationMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(organizationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Mono<OrganizationResponse> getOrganizationById(Long organizationId) {
        return Mono.fromSupplier(() -> this.organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found with Id: " + organizationId)))
                .map(organizationMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }
}