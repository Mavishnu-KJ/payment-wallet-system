package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.WalletResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "wallet-service", url = "${wallet-service.url:http://localhost:8082}")
public interface WalletServiceClient {

    @GetMapping("/api/wallets/internal/{walletId}")
    WalletResponseDto getWalletById(@PathVariable Long walletId);

    @PostMapping("/api/wallets/internal/{walletId}/debit")
    WalletResponseDto debit(@PathVariable Long walletId, @RequestBody BigDecimal amount);

    @PostMapping("/api/wallets/internal/{walletId}/credit")
    WalletResponseDto credit(@PathVariable Long walletId, @RequestBody BigDecimal amount);

}
