package com.example.walletservice.model.dto;

import lombok.Data;

@Data
public class WalletResponseDto {
    private Long id;
    private Long userId;
    private Double balance;
    private String currency;
}
