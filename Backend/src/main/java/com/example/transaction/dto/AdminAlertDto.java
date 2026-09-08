package com.example.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminAlertDto {
    private Long id;
    private Long transactionId;
    private String reason;
    private boolean reviewed;
    private LocalDateTime createdAt;
    private String severity;
    private String source;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
}

