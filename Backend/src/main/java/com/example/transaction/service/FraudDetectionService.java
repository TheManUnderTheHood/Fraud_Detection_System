package com.example.transaction.service;

import com.example.transaction.Repository.FraudAlertRepository;
import com.example.transaction.Repository.TransactionRepository;
import com.example.transaction.entity.Account;
import com.example.transaction.entity.FraudAlert;
import com.example.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    private final TransactionRepository transactionRepository;
    private final FraudAlertRepository fraudAlertRepository;
    public String evaluateTransaction(Account sender, Account receiver, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            return "LARGE_TRANSACTION: Amount exceeds $100,000 threshold.";
        }
        BigDecimal eightyPercentOfBalance = sender.getBalance().multiply(new BigDecimal("0.80"));
        if (amount.compareTo(eightyPercentOfBalance) > 0) {
            return "LOW_BALANCE_BURST: Attempting to transfer more than 80% of account balance.";
        }
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long recentTxCount = transactionRepository.countRecentTransactions(sender.getId(), oneMinuteAgo);
        if (recentTxCount >= 5) {
            return "RAPID_TRANSACTIONS: More than 5 transactions in the last 60 seconds.";
        }
        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        long circularTxCount = transactionRepository.countTransactionsBetweenAccounts(sender.getId(), receiver.getId(), oneDayAgo);
        if (circularTxCount >= 3) {
            return "CIRCULAR_TRANSFER: Repeated transfers between the same two accounts in 24 hours.";
        }
        return null;
    }
    public void createFraudAlert(Transaction savedTransaction, String reason) {
        FraudAlert alert = FraudAlert.builder()
                .transaction(savedTransaction)
                .reason(reason)
                .reviewed(false)
                .build();
        fraudAlertRepository.save(alert);
    }
}
