package com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.service;

import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.entity.Organization;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.repository.AddressRepository;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.db.repository.OrganizationRepository;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.mapper.OrganizationMapper;
import com.traditional.databases.jdbcpostgresqlonetwoonebidirectionalrelation.web.model.request.OrganizationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationMapper organizationMapper;
    private final OrganizationRepository organizationRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public Mono<Organization> createOrganization(final Mono<OrganizationRequest> organizationMono) {
        return organizationMono
                .map(organizationMapper::toEntity)
                .map(organizationRepository::save)
                .subscribeOn(Schedulers.boundedElastic());
    }
}