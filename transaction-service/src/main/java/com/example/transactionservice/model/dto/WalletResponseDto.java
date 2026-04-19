package com.example.transactionservice.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletResponseDto {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private String currency;
}
