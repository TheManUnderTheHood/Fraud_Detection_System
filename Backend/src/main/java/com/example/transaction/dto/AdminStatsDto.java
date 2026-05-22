package com.example.transaction.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminStatsDto {
    private long totalTransactions;
    private long totalFraudAlerts;
    private long pendingAlerts;
    private long reviewedAlerts;
}
