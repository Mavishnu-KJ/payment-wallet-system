package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.WalletResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "wallet-service", url = "${wallet-service.url:http://localhost:8082}")
public interface WalletServiceClient {

    @GetMapping("/api/wallets/internal/{walletId}")
    WalletResponseDto getWalletById(@PathVariable Long walletId);

    @PostMapping("/api/wallets/internal/{walletId}/debit")
    WalletResponseDto debit(@PathVariable Long walletId, @RequestBody BigDecimal amount);

    @PostMapping("/api/wallets/internal/{walletId}/credit")
    WalletResponseDto credit(@PathVariable Long walletId, @RequestBody BigDecimal amount);

    @GetMapping("api/wallets/internal/user/{userId}")
    WalletResponseDto getWalletByUserId(@PathVariable("userId") Long userId);

    @PostMapping("/api/wallets/internal/{walletId}/acquireLock")
    boolean acquireLock(@PathVariable Long walletId, @RequestParam(defaultValue = "60") long timeoutSeconds);

    @PostMapping("/api/wallets/internal/{walletId}/releaseLock")
    Void releaseLock(@PathVariable Long walletId);

}
