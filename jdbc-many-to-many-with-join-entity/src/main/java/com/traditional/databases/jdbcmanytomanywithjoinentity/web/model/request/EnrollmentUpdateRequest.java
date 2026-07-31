package com.traditional.databases.jdbcmanytomanywithjoinentity.web.model.request;

import com.traditional.databases.jdbcmanytomanywithjoinentity.db.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentUpdateRequest {
    private Instant enrolledAt;
    private EnrollmentStatus status;
    private BigDecimal grade;
    private String createdBy;
}

