package com.example.walletservice.model.dto;

import com.example.walletservice.model.enums.WalletStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletResponseDto {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
}
