package com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.controller;

import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.service.FetchStrategyDemoService;
import com.traditional.databases.jdbcfetchstrategiesnplus1queryproblem.web.model.response.FetchStrategyReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FetchStrategyController {

    private final FetchStrategyDemoService fetchStrategyDemoService;

    @GetMapping("/fetch-demo/author/{authorId}/lazy")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> oneParentLazy(@PathVariable Long authorId) {
        return fetchStrategyDemoService.oneParentLazy(authorId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/author/{authorId}/join-fetch")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> oneParentJoinFetch(@PathVariable Long authorId) {
        return fetchStrategyDemoService.oneParentJoinFetch(authorId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/author/{authorId}/entity-graph")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> oneParentEntityGraph(@PathVariable Long authorId) {
        return fetchStrategyDemoService.oneParentEntityGraph(authorId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/author/{authorId}/join-fetch/deep")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> oneParentJoinFetchDeep(@PathVariable Long authorId) {
        return fetchStrategyDemoService.oneParentJoinFetchDeep(authorId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/author/{authorId}/entity-graph/deep")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> oneParentEntityGraphDeep(@PathVariable Long authorId) {
        return fetchStrategyDemoService.oneParentEntityGraphDeep(authorId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/authors/lazy")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> manyParentsLazy() {
        return fetchStrategyDemoService.manyParentsLazy()
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/authors/join-fetch")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> manyParentsJoinFetch() {
        return fetchStrategyDemoService.manyParentsJoinFetch()
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/authors/entity-graph")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> manyParentsEntityGraph() {
        return fetchStrategyDemoService.manyParentsEntityGraph()
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/authors/join-fetch/deep")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> manyParentsJoinFetchDeep() {
        return fetchStrategyDemoService.manyParentsJoinFetchDeep()
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/authors/entity-graph/deep")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> manyParentsEntityGraphDeep() {
        return fetchStrategyDemoService.manyParentsEntityGraphDeep()
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/fetch-demo/books/eager")
    public Mono<ResponseEntity<FetchStrategyReportResponse>> manyBooksEager() {
        return fetchStrategyDemoService.manyBooksWithEagerAuthor()
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

