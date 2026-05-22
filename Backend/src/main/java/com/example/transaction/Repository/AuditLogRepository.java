package com.example.transaction.Repository;


import com.example.transaction.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Standard CRUD operations are enough for the audit log right now.
}
