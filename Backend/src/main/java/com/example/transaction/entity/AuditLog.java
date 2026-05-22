package com.example.transaction.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String performedBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    // Explicit methods keep runtime behavior stable if Lombok processing is inconsistent.
    public void setAction(String action) {
        this.action = action;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
}
