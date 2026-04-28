package com.example.walletservice.service;

import com.example.walletservice.model.dto.AddMoneyRequestDto;
import com.example.walletservice.model.dto.WalletResponseDto;

import java.math.BigDecimal;

public interface WalletService {
    WalletResponseDto addMoney(AddMoneyRequestDto addMoneyRequestDto);
    WalletResponseDto getMyWallet();
    BigDecimal getMyWalletBalance();

    //Internal API methods
    WalletResponseDto getWalletById(Long walletId);
    WalletResponseDto debit(Long walletId, BigDecimal amount);
    WalletResponseDto credit(Long walletId, BigDecimal amount);
    WalletResponseDto getWalletByUserId(Long userId);

    //New cache-related methods - added for Redis
    WalletResponseDto getWalletWithCache(Long userId);
    void evictWalletCache(Long userId);

}