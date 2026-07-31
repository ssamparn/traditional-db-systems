package com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.controller;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.service.MemberService;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.MemberResponse;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.TeamMemberResponse;
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
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/member/create/team/{teamId}")
    public Mono<ResponseEntity<MemberResponse>> createMember(@PathVariable Long teamId,
                                                             @RequestBody MemberRequest request) {
        return memberService.createMember(teamId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.CREATED));
    }

    @GetMapping("/member/get/{memberId}")
    public Mono<ResponseEntity<MemberResponse>> getMember(@PathVariable Long memberId) {
        return memberService.getMemberById(memberId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/member/get/all")
    public Flux<MemberResponse> getMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/team/member/info")
    public Flux<TeamMemberResponse> getTeamMemberInfo() {
        return memberService.getTeamMemberInfo();
    }

    @PutMapping("/member/update/{memberId}")
    public Mono<ResponseEntity<MemberResponse>> updateMember(@PathVariable Long memberId,
                                                             @RequestBody MemberRequest request) {
        return memberService.updateMember(memberId, request)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @PutMapping("/member/reassign/{memberId}/team/{teamId}")
    public Mono<ResponseEntity<MemberResponse>> reassignMemberTeam(@PathVariable Long memberId,
                                                                   @PathVariable Long teamId) {
        return memberService.reassignMemberTeam(memberId, teamId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }

    @DeleteMapping("/member/delete/{memberId}")
    public Mono<ResponseEntity<MemberResponse>> deleteMember(@PathVariable Long memberId) {
        return memberService.deleteMember(memberId)
                .map(response -> new ResponseEntity<>(response, HttpStatus.OK));
    }
}

