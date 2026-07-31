package com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.response;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private Instant enrolledAt;
    private EnrollmentStatus status;
    private BigDecimal grade;
    private String createdBy;
    private StudentSummaryResponse student;
    private CourseSummaryResponse course;
}

