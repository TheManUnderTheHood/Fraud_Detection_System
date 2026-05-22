package com.example.transaction.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.transaction.enums.TransactionStatus;
import jakarta.persistence .*;
import lombok .*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Entity
    @Table(name = "transactions")

    public class Transaction {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "sender_account_id", nullable = false)
        @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
        private Account senderAccount;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "receiver_account_id", nullable = false)
        @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
        private Account receiverAccount;

        @Column(nullable = false, precision = 15, scale = 2)
        private BigDecimal amount;

        @CreationTimestamp
        @Column(updatable = false)
        private LocalDateTime timestamp;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TransactionStatus status;

        @Column(nullable = false)
        private boolean flagged;
    }
