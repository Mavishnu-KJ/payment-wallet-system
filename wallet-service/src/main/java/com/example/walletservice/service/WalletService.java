package com.example.walletservice.service;

import com.example.walletservice.model.dto.AddMoneyRequestDto;
import com.example.walletservice.model.dto.WalletResponseDto;

public interface WalletService {
    WalletResponseDto addMoney(AddMoneyRequestDto addMoneyRequestDto);
    WalletResponseDto getMyWallet();
    Double getMyWalletBalance();
}