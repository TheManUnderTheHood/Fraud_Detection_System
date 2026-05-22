package com.example.transaction.service;

import com.example.transaction.Repository.AccountRepository;
import com.example.transaction.Repository.UserRepository;
import com.example.transaction.entity.Account;
import com.example.transaction.entity.User;
import com.example.transaction.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    @Transactional
    public Account createAccount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = new Account();
        account.setUser(user);
        account.setBalance(new BigDecimal("10000.00"));
        Account savedAccount = accountRepository.save(account);

        auditService.logAction("ACCOUNT_CREATED_ID_" + savedAccount.getId(), userEmail);

        return savedAccount;
    }
    public List<Account> getUserAccounts(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return accountRepository.findByUserId(user.getId());
    }
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));
    }
}
