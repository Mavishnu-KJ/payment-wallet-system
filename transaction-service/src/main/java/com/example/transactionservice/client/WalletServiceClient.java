package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.WalletResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

//@FeignClient(name = "wallet-service", url = "${wallet-service.url:http://localhost:8082}")
//@FeignClient(name = "wallet-service", url = "http://wallet-service:8080") //Changed url because of docker
//@FeignClient(name = "wallet-service") //Just service name enough, Eureka service discovery wil tc
@FeignClient(name = "wallet-service", fallback = WalletServiceClientFallback.class)
public interface WalletServiceClient {

    @GetMapping("/api/wallets/internal/{walletId}")
    @CircuitBreaker(name = "walletServiceCircuit")
    WalletResponseDto getWalletById(@PathVariable Long walletId);

    @PostMapping("/api/wallets/internal/{walletId}/debit")
    @CircuitBreaker(name = "walletServiceCircuit")
    WalletResponseDto debit(@PathVariable Long walletId, @RequestBody BigDecimal amount);

    @PostMapping("/api/wallets/internal/{walletId}/credit")
    @CircuitBreaker(name = "walletServiceCircuit")
    WalletResponseDto credit(@PathVariable Long walletId, @RequestBody BigDecimal amount);

    @GetMapping("api/wallets/internal/user/{userId}")
    @CircuitBreaker(name = "walletServiceCircuit")
    WalletResponseDto getWalletByUserId(@PathVariable("userId") Long userId);

    @PostMapping("/api/wallets/internal/{walletId}/acquireLock")
    @CircuitBreaker(name = "walletServiceCircuit")
    boolean acquireLock(@PathVariable Long walletId, @RequestParam(defaultValue = "60") long timeoutSeconds);

    @PostMapping("/api/wallets/internal/{walletId}/releaseLock")
    @CircuitBreaker(name = "walletServiceCircuit")
    Void releaseLock(@PathVariable Long walletId);

}
