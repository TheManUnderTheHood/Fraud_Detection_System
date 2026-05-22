package com.example.transaction.controller;

import com.example.transaction.Repository.FraudAlertRepository;
import com.example.transaction.Repository.TransactionRepository;
import com.example.transaction.dto.AdminAlertDto;
import com.example.transaction.dto.AdminStatsDto;
import com.example.transaction.entity.FraudAlert;
import com.example.transaction.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TransactionRepository transactionRepository;
    private final FraudAlertRepository fraudAlertRepository;

    @GetMapping("/alerts")
    public ResponseEntity<List<AdminAlertDto>> getPendingAlerts() {
        return ResponseEntity.ok(adminService.getUnreviewedAlerts());
    }

    @PutMapping("/alerts/{alertId}/review")
    public ResponseEntity<FraudAlert> reviewAlert(@PathVariable Long alertId, Authentication authentication) {
        String adminEmail = authentication.getName();
        return ResponseEntity.ok(adminService.markAlertAsReviewed(alertId, adminEmail));
    }

    @GetMapping("/statistics")
    public ResponseEntity<AdminStatsDto> getStatistics() {
        long totalTx = transactionRepository.count();
        long totalAlerts = fraudAlertRepository.count();
        long pending = fraudAlertRepository.findByReviewedFalse().size();

        AdminStatsDto stats = AdminStatsDto.builder()
                .totalTransactions(totalTx)
                .totalFraudAlerts(totalAlerts)
                .pendingAlerts(pending)
                .reviewedAlerts(totalAlerts - pending)
                .build();

        return ResponseEntity.ok(stats);
    }
}