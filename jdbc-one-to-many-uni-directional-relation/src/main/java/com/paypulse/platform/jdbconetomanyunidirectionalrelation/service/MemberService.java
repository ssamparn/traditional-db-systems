package com.paypulse.platform.jdbconetomanyunidirectionalrelation.service;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.MemberRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.repository.TeamRepository;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.mapper.MemberMapper;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.exception.ResourceNotFoundException;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.MemberResponse;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.TeamMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public Mono<MemberResponse> createMember(Long teamId, MemberRequest request) {
        return Mono.fromSupplier(() -> teamRepository.findByIdWithMembers(teamId)
                        .orElseThrow(() -> new ResourceNotFoundException("Team not found with Id: " + teamId)))
                .doOnNext(ignored -> MemberRequestValidator.validate(request))
                .map(team -> {
                    Member transientMember = memberMapper.toEntity(request);
                    team.addMember(transientMember);

                    Team persistedTeam = teamRepository.saveAndFlush(team);
                    return persistedTeam.getMembers().stream()
                            .filter(member -> request.getEmail().equals(member.getEmail()))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Member not found after create for email: " + request.getEmail()));
                })
                .map(member -> memberMapper.toResponse(member, teamId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<MemberResponse> getMemberById(Long memberId) {
        return findById(memberId)
                .map(member -> memberMapper.toResponse(member, resolveTeamId(member.getId())))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<MemberResponse> getAllMembers() {
        return Flux.fromIterable(memberRepository.findAll())
                .map(member -> memberMapper.toResponse(member, resolveTeamId(member.getId())))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<TeamMemberResponse> getTeamMemberInfo() {
        return Flux.fromIterable(teamRepository.getJoinInformation())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<MemberResponse> updateMember(Long memberId, MemberRequest request) {
        return findById(memberId)
                .doOnNext(ignored -> MemberRequestValidator.validate(request))
                .map(member -> memberMapper.updateEntity(member, request))
                .map(memberRepository::save)
                .map(member -> memberMapper.toResponse(member, resolveTeamId(member.getId())))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<MemberResponse> reassignMemberTeam(Long memberId, Long teamId) {
        return Mono.fromSupplier(() -> {
                    memberRepository.findById(memberId)
                            .orElseThrow(() -> new ResourceNotFoundException("Member not found with Id: " + memberId));
                    teamRepository.findById(teamId)
                            .orElseThrow(() -> new ResourceNotFoundException("Team not found with Id: " + teamId));
                    int updated = memberRepository.reassignMemberToTeam(memberId, teamId);
                    if (updated == 0) {
                        throw new ResourceNotFoundException("Member not found with Id: " + memberId);
                    }
                    return memberRepository.findById(memberId)
                            .orElseThrow(() -> new ResourceNotFoundException("Member not found with Id: " + memberId));
                })
                .map(member -> memberMapper.toResponse(member, teamId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<MemberResponse> deleteMember(Long memberId) {
        return findById(memberId)
                .map(member -> {
                    Long teamId = resolveTeamId(member.getId());
                    memberRepository.delete(member);
                    return memberMapper.toResponse(member, teamId);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Member> findById(Long memberId) {
        return Mono.fromSupplier(() -> memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found with Id: " + memberId)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Long resolveTeamId(Long memberId) {
        return teamRepository.findTeamIdByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found for Member Id: " + memberId));
    }
}

