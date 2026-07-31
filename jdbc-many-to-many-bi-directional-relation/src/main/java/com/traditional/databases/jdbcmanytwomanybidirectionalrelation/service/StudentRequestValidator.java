package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.StudentRequest;

import java.util.regex.Pattern;

public final class StudentRequestValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private StudentRequestValidator() {
    }

    public static void validate(StudentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getFirstName(), "firstName", 80);
        requireText(request.getLastName(), "lastName", 80);
        requireText(request.getMobile(), "mobile", 20);
        requireText(request.getEmail(), "email", 128);

        if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            throw new IllegalArgumentException("email must be a valid email address");
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


