package com.example.transaction.service;


import com.example.transaction.Repository.AuditLogRepository;
import com.example.transaction.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    public void logAction(String action, String performedBy) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setPerformedBy(performedBy);
        auditLogRepository.save(log);
    }
}
