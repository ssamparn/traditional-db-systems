package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.controller;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.service.TeamService;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.TeamRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.TeamResponse;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/team/create")
    public Mono<ResponseEntity<TeamResponse>> createTeam(@RequestBody TeamRequest request) {
        return teamService.createTeam(request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/team/get/all")
    public Flux<TeamResponse> getTeams() {
        return teamService.getAllTeams();
    }

    @GetMapping("/team/get/{teamId}")
    public Mono<ResponseEntity<TeamResponse>> getTeam(@PathVariable Long teamId) {
        return teamService.getTeamById(teamId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @PutMapping("/team/update/{teamId}")
    public Mono<ResponseEntity<TeamResponse>> updateTeam(@PathVariable Long teamId,
                                                         @RequestBody TeamRequest request) {
        return teamService.updateTeam(teamId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/team/delete/{teamId}")
    public Mono<ResponseEntity<TeamResponse>> deleteTeam(@PathVariable Long teamId) {
        return teamService.deleteTeam(teamId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

