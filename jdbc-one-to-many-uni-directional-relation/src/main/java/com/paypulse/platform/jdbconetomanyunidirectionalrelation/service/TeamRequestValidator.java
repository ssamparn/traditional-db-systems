package com.paypulse.platform.jdbconetomanyunidirectionalrelation.service;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;
import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.TeamRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class TeamRequestValidator {

    private TeamRequestValidator() {
    }

    static void validate(TeamRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getTeamCode(), "teamCode", 64);
        requireText(request.getName(), "name", 120);
        requireText(request.getDescription(), "description", 240);

        List<MemberRequest> members = request.getMembers();
        if (members == null || members.isEmpty()) {
            return;
        }

        Set<String> normalizedEmails = new HashSet<>();
        for (int i = 0; i < members.size(); i++) {
            MemberRequest memberRequest = members.get(i);
            String prefix = "members[" + i + "]";
            MemberRequestValidator.validate(memberRequest, prefix);

            String normalizedEmail = memberRequest.getEmail().trim().toLowerCase(Locale.ROOT);
            if (!normalizedEmails.add(normalizedEmail)) {
                throw new IllegalArgumentException("Duplicate email in request: " + memberRequest.getEmail());
            }
        }
    }

    private static void requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxLength + " characters");
        }
    }
}

