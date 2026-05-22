package com.example.transaction.Repository;

import com.example.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    List<Transaction> findBySenderAccountIdOrReceiverAccountId(Long senderId, Long receiverId);

    // JPQL queries the Java Objects, not the database tables.

    // 1. For "Rapid Transactions" Fraud Rule: Count how many transactions an account made since a specific time.
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.senderAccount.id = :accountId AND t.timestamp >= :timeLimit")
    long countRecentTransactions(@Param("accountId") Long accountId, @Param("timeLimit") LocalDateTime timeLimit);

    // 2. For "Circular Transfer" Fraud Rule: Find how many times Account A sent money to Account B since a specific time.
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.senderAccount.id = :senderId AND t.receiverAccount.id = :receiverId AND t.timestamp >= :timeLimit")
    long countTransactionsBetweenAccounts(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId, @Param("timeLimit") LocalDateTime timeLimit);
}

