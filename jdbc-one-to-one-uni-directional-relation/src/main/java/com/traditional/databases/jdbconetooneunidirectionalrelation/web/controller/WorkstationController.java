package com.traditional.databases.jdbconetooneunidirectionalrelation.web.controller;

import com.traditional.databases.jdbconetooneunidirectionalrelation.db.repository.WorkstationRepository;
import com.traditional.databases.jdbconetooneunidirectionalrelation.mapper.WorkstationMapper;
import com.traditional.databases.jdbconetooneunidirectionalrelation.web.model.response.WorkstationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WorkstationController {

    private final WorkstationMapper workstationMapper;
    private final WorkstationRepository workstationRepository;

    @GetMapping("/workstation/get/all")
    public Flux<WorkstationResponse> getWorkstations() {
        return Flux.fromIterable(workstationRepository.findAll())
                .map(workstationMapper::toResponse)
                .subscribeOn(Schedulers.boundedElastic());
    }
}

