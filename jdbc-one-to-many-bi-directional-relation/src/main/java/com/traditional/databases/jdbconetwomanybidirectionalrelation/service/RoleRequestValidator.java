package com.traditional.databases.jdbconetwomanybidirectionalrelation.service;

import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.RoleRequest;
import com.traditional.databases.jdbconetwomanybidirectionalrelation.web.model.request.UserRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class RoleRequestValidator {

    private RoleRequestValidator() {
    }

    static void validate(RoleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getName(), "name", 80);
        requireText(request.getDescription(), "description", 240);

        List<UserRequest> users = request.getUsers();
        if (users == null || users.isEmpty()) {
            return;
        }

        Set<String> normalizedEmails = new HashSet<>();
        for (int i = 0; i < users.size(); i++) {
            UserRequest user = users.get(i);
            String prefix = "users[" + i + "]";
            UserRequestValidator.validate(user, prefix);

            String normalizedEmail = user.getEmail().trim().toLowerCase(Locale.ROOT);
            if (!normalizedEmails.add(normalizedEmail)) {
                throw new IllegalArgumentException("Duplicate email in request: " + user.getEmail());
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

