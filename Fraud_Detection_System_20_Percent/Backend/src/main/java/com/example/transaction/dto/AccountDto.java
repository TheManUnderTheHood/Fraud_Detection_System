package com.example.transaction.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AccountDto {
    private Long id;
    private BigDecimal balance;
}