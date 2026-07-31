package com.paypulse.platform.jdbconetomanyunidirectionalrelation.mapper;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.MemberResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public Member toEntity(MemberRequest request) {
        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        member.setMobile(request.getMobile());
        return member;
    }

    public Member updateEntity(Member member, MemberRequest request) {
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        member.setMobile(request.getMobile());
        return member;
    }

    public MemberResponse toResponse(Member member, Long teamId) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setEmail(member.getEmail());
        response.setMobile(member.getMobile());
        response.setTeamId(teamId);
        return response;
    }
}

