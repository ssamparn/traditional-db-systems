package com.paypulse.platform.jdbconetomanyunidirectionalrelation.service;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.TeamRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.mapper.TeamMapper;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.exception.ResourceNotFoundException;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.TeamRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamMapper teamMapper;
    private final TeamRepository teamRepository;

    @Transactional
    public Mono<TeamResponse> createTeam(TeamRequest request) {
        return Mono.just(request)
                .doOnNext(TeamRequestValidator::validate)
                .map(teamMapper::toEntity)
                .map(teamRepository::save)
                .map(teamMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<TeamResponse> getAllTeams() {
        return Flux.fromIterable(teamRepository.findAllWithMembers())
                .map(teamMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TeamResponse> getTeamById(Long teamId) {
        return findByIdWithMembers(teamId)
                .map(teamMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<TeamResponse> updateTeam(Long teamId, TeamRequest request) {
        return findByIdWithMembers(teamId)
                .doOnNext(ignored -> TeamRequestValidator.validate(request))
                .map(team -> teamMapper.updateEntity(team, request))
                .map(teamRepository::save)
                .map(teamMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<TeamResponse> deleteTeam(Long teamId) {
        return findByIdWithMembers(teamId)
                .map(team -> {
                    teamRepository.delete(team);
                    return team;
                })
                .map(teamMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Team> findById(Long teamId) {
        return Mono.fromSupplier(() -> teamRepository.findById(teamId)
                        .orElseThrow(() -> new ResourceNotFoundException("Team not found with Id: " + teamId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Team> findByIdWithMembers(Long teamId) {
        return Mono.fromSupplier(() -> teamRepository.findByIdWithMembers(teamId)
                        .orElseThrow(() -> new ResourceNotFoundException("Team not found with Id: " + teamId)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}

