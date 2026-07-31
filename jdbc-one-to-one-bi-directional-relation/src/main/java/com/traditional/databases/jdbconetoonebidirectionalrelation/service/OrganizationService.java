package com.traditional.databases.jdbconetoonebidirectionalrelation.service;

import com.traditional.databases.jdbconetoonebidirectionalrelation.db.entity.Organization;
import com.traditional.databases.jdbconetoonebidirectionalrelation.db.repository.OrganizationRepository;
import com.traditional.databases.jdbconetoonebidirectionalrelation.mapper.OrganizationMapper;
import com.traditional.databases.jdbconetoonebidirectionalrelation.web.exception.ResourceNotFoundException;
import com.traditional.databases.jdbconetoonebidirectionalrelation.web.model.request.OrganizationRequest;
import com.traditional.databases.jdbconetoonebidirectionalrelation.web.model.response.OrganizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationMapper organizationMapper;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public Mono<OrganizationResponse> createOrganization(final OrganizationRequest organizationRequest) {
        return Mono.just(organizationRequest)
                .doOnNext(OrganizationRequestValidator::validate)
                .map(organizationMapper::toEntity)
                .map(organizationRepository::save)
                .map(organizationMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<OrganizationResponse> getAllOrganizations() {
        return Flux.fromIterable(this.organizationRepository.findAll())
                .map(organizationMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<OrganizationResponse> getOrganizationById(Long organizationId) {
        return Mono.fromSupplier(() -> this.organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Organization not found with Id: " + organizationId)))
                .map(organizationMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<OrganizationResponse> updateOrganization(Long organizationId, OrganizationRequest request) {
        return Mono.fromSupplier(() -> this.organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with Id: " + organizationId)))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(ignored -> OrganizationRequestValidator.validate(request))
                .map(organization -> organizationMapper.updateEntity(organizationId, organization, request))
                .map(organizationRepository::save)
                .map(organizationMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<OrganizationResponse> deleteOrganizationById(Long organizationId) {
        return this.findById(organizationId)
                .publishOn(Schedulers.boundedElastic())
                .map(organization -> {
                    this.organizationRepository.delete(organization);
                    return organization;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(organizationMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Organization> findById(Long organizationId) {
        return Mono.fromSupplier(() -> this.organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Organization not found with Id: " + organizationId)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}