package com.example.transaction.service;

import com.example.transaction.Repository.FraudAlertRepository;
import com.example.transaction.Repository.TransactionRepository;
import com.example.transaction.dto.AdminAlertDto;
import com.example.transaction.dto.AdminStatsDto;
import com.example.transaction.entity.FraudAlert;
import com.example.transaction.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FraudAlertRepository fraudAlertRepository;
    private final AuditService auditService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public List<AdminAlertDto> getUnreviewedAlerts() {
        return fraudAlertRepository.findByReviewedFalse().stream()
                .map(alert -> AdminAlertDto.builder()
                        .id(alert.getId())
                        .transactionId(alert.getTransaction().getId())
                        .reason(alert.getReason())
                        .reviewed(alert.isReviewed())
                        .createdAt(alert.getCreatedAt())
                        .build())
                .toList();
    }

    public FraudAlert markAlertAsReviewed(Long alertId, String adminEmail) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found"));
        alert.setReviewed(true);
        FraudAlert savedAlert = fraudAlertRepository.save(alert);
        auditService.logAction("FRAUD_REVIEWED_ID_" + alert.getId(), adminEmail);

        return savedAlert;
    }
    public AdminStatsDto getSystemStatistics(long totalTransactions) {
        long totalAlerts = fraudAlertRepository.count();
        long pending = fraudAlertRepository.findByReviewedFalse().size();

        return AdminStatsDto.builder()
                .totalTransactions(totalTransactions)
                .totalFraudAlerts(totalAlerts)
                .pendingAlerts(pending)
                .reviewedAlerts(totalAlerts - pending)
                .build();
    }
}
