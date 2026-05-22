package com.example.transaction.service;

import com.example.transaction.Repository.AccountRepository;
import com.example.transaction.Repository.TransactionRepository;
import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.entity.Account;
import com.example.transaction.entity.Transaction;
import com.example.transaction.enums.TransactionStatus;
import com.example.transaction.exception.InsufficientBalanceException;
import com.example.transaction.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AuditService auditService;
    private final AiAgentService aiAgentService;

    @Transactional
    public Transaction processTransfer(TransactionRequest request, String userEmail) {
        Account sender = accountRepository.findById(request.getSenderAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));
        if (!sender.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have permission to transfer from this account");
        }

        Account receiver = accountRepository.findById(request.getReceiverAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in sender account");
        }
        String fraudReason = fraudDetectionService.evaluateTransaction(sender, receiver, request.getAmount());
        boolean isFlagged = (fraudReason != null);


        Transaction transaction = Transaction.builder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(request.getAmount())
                .status(TransactionStatus.SUCCESS)
                .flagged(isFlagged)
                .build();

        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // 8. Save the transaction to the database
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 9. Handle Fraud Alerts & AI Verification
        if (isFlagged) {
            // Hardcoded Rule Caught It!
            fraudDetectionService.createFraudAlert(savedTransaction, fraudReason);
            auditService.logAction("FRAUD_FLAGGED_TX_" + savedTransaction.getId(), "SYSTEM");
        } else {
            // Normal logging for clean transactions
            auditService.logAction("TRANSACTION_SUCCESS_TX_" + savedTransaction.getId(), userEmail);

            // 🔥 WAKE UP THE AI AGENT 🔥
            // Even though our hardcoded rules said it's clean, we ask the AI to double-check for anomalies in the background!
            aiAgentService.analyzeTransactionContext(savedTransaction, sender);
        }

        return savedTransaction;
    }
    public List<Transaction> getTransactionHistory(Long accountId, String userEmail) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Unauthorized access to account history");
        }

        return transactionRepository.findBySenderAccountIdOrReceiverAccountId(accountId, accountId);
    }
}
