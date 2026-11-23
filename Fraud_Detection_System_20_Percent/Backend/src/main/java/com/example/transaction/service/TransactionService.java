package com.example.transaction.service;

import com.example.transaction.Repository.AccountRepository;
import com.example.transaction.Repository.TransactionRepository;
import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.dto.TransactionDto;
import com.example.transaction.entity.Account;
import com.example.transaction.entity.Transaction;
import com.example.transaction.enums.TransactionStatus;
import com.example.transaction.exception.InsufficientBalanceException;
import com.example.transaction.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.example.transaction.exception.BadRequestException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AuditService auditService;

    @Transactional
    public Transaction processTransfer(TransactionRequest request, String userEmail) {
        if (request.getSenderAccountId().equals(request.getReceiverAccountId())) {
            throw new BadRequestException("Sender and receiver accounts must be different");
        }
        if (request.getAmount().scale() > 2) {
            throw new BadRequestException("Transfer amount must use at most two decimal places");
        }
        if (request.getIdempotencyKey() != null) {
            var previous = transactionRepository.findByInitiatedByAndIdempotencyKey(userEmail, request.getIdempotencyKey());
            if (previous.isPresent()) {
                return previous.get();
            }
        }
        Long firstLockId = Math.min(request.getSenderAccountId(), request.getReceiverAccountId());
        Long secondLockId = Math.max(request.getSenderAccountId(), request.getReceiverAccountId());
        Account firstLockedAccount = accountRepository.findByIdForUpdate(firstLockId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender account not found"));
        Account secondLockedAccount = accountRepository.findByIdForUpdate(secondLockId)
            .orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));
        Account sender = request.getSenderAccountId().equals(firstLockId) ? firstLockedAccount : secondLockedAccount;
        if (!sender.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You do not have permission to transfer from this account");
        }
        Account receiver = request.getReceiverAccountId().equals(firstLockId) ? firstLockedAccount : secondLockedAccount;

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
                .idempotencyKey(request.getIdempotencyKey())
                .initiatedBy(userEmail)
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
        }

        return savedTransaction;
    }
    public List<TransactionDto> getTransactionHistory(Long accountId, String userEmail) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Unauthorized access to account history");
        }

        return transactionRepository.findBySenderAccountIdOrReceiverAccountId(accountId, accountId)
                .stream().map(this::toDto).toList();
    }

    public TransactionDto toDto(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .senderAccountId(transaction.getSenderAccount().getId())
                .receiverAccountId(transaction.getReceiverAccount().getId())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .flagged(transaction.isFlagged())
                .build();
    }
}
