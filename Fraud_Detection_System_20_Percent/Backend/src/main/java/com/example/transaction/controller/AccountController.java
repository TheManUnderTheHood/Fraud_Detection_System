package com.example.transaction.controller;

import com.example.transaction.entity.Account;
import com.example.transaction.dto.AccountDto;
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
    public ResponseEntity<AccountDto> createAccount(Authentication authentication) {
        String userEmail = authentication.getName();
        Account newAccount = accountService.createAccount(userEmail);
        return ResponseEntity.ok(accountService.toDto(newAccount));
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<List<AccountDto>> getMyAccounts(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(accountService.getUserAccounts(userEmail).stream().map(accountService::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(accountService.toDto(accountService.getAccountById(id, authentication.getName())));
    }
}