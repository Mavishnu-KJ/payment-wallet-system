package com.example.transactionservice.model.dto;

import com.example.transactionservice.model.enums.TransactionStatus;
import com.example.transactionservice.model.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponseDto {
    private String transactionId;
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private LocalDateTime transactionDate;
}
