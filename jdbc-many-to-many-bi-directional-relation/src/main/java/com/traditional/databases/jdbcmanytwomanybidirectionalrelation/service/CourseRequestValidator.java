package com.traditional.databases.jdbcmanytwomanybidirectionalrelation.service;

import com.traditional.databases.jdbcmanytwomanybidirectionalrelation.web.model.request.CourseRequest;

public final class CourseRequestValidator {

    private CourseRequestValidator() {
    }

    public static void validate(CourseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireText(request.getName(), "name", 80);
        requireText(request.getDescription(), "description", 240);
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


