package com.paypulse.platform.jdbconetomanyunidirectionalrelation.mapper;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Member;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.db.entity.Team;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.TeamRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.MemberResponse;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.response.TeamResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamMapper {

    private final MemberMapper memberMapper;

    public Team toEntity(TeamRequest request) {
        Team team = new Team();
        applyRequest(team, request);
        return team;
    }

    public Team updateEntity(Team team, TeamRequest request) {
        applyRequest(team, request);
        return team;
    }

    private void applyRequest(Team team, TeamRequest request) {
        team.setTeamCode(request.getTeamCode());
        team.setName(request.getName());
        team.setDescription(request.getDescription());

        List<Member> existingMembers = new ArrayList<>(team.getMembers());
        existingMembers.forEach(team::removeMember);

        List<Member> members = createMembers(request.getMembers());
        members.forEach(team::addMember);
    }

    private List<Member> createMembers(List<MemberRequest> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream().map(memberMapper::toEntity).toList();
    }

    public TeamResponse toResponse(Team team) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setTeamCode(team.getTeamCode());
        response.setName(team.getName());
        response.setDescription(team.getDescription());

        List<MemberResponse> members = team.getMembers() == null
                ? Collections.emptyList()
                : team.getMembers().stream().map(member -> memberMapper.toResponse(member, team.getId())).toList();
        response.setMembers(members);
        return response;
    }
}

