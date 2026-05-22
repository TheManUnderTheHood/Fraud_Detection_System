package com.example.transaction.controller;

import com.example.transaction.entity.Account;
import com.example.transaction.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<Account> createAccount(Authentication authentication) {
        String userEmail = authentication.getName();
        Account newAccount = accountService.createAccount(userEmail);
        return ResponseEntity.ok(newAccount);
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<List<Account>> getMyAccounts(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(accountService.getUserAccounts(userEmail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }
}