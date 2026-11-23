package com.example.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequest {
    @NotNull(message = "Sender account ID is required")
    private Long senderAccountId;

    @NotNull(message = "Receiver account ID is required")
    private Long receiverAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Transfer amount must be greater than zero") 
    @Digits(integer = 13, fraction = 2, message = "Transfer amount must have at most 2 decimal places")
    private BigDecimal amount;

    @Size(max = 100, message = "Idempotency key is too long")
    private String idempotencyKey;
}
