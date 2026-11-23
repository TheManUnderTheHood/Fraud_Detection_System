package com.example.transaction.entity;


import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private boolean reviewed;

    @Column(length = 20)
    private String severity;

    @Column(length = 320)
    private String reviewedBy;

    private LocalDateTime reviewedAt;

    @Column(length = 20, nullable = false)
    private String source;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
