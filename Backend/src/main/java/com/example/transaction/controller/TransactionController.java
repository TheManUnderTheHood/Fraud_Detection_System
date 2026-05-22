package com.example.transaction.controller;

import com.example.transaction.dto.TransactionRequest;
import com.example.transaction.entity.Transaction;
import com.example.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<Transaction> transferMoney(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        Transaction processedTransaction = transactionService.processTransfer(request, userEmail);
        return ResponseEntity.ok(processedTransaction);
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(
            @PathVariable Long accountId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountId, userEmail));
    }
}