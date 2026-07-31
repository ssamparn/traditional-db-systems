package com.paypulse.platform.jdbconetomanyunidirectionalrelation.service;

import com.paypulse.platform.jdbconetomanyunidirectionalrelation.web.model.request.MemberRequest;

import java.util.regex.Pattern;

final class MemberRequestValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private MemberRequestValidator() {
    }

    static void validate(MemberRequest request) {
        validate(request, "member");
    }

    static void validate(MemberRequest request, String prefix) {
        if (request == null) {
            throw new IllegalArgumentException(prefix + " is required");
        }

        requireText(request.getFirstName(), prefix + ".firstName", 80);
        requireText(request.getLastName(), prefix + ".lastName", 80);
        requireText(request.getEmail(), prefix + ".email", 128);
        requireText(request.getMobile(), prefix + ".mobile", 20);

        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new IllegalArgumentException(prefix + ".email must be a valid email address");
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

