package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.WalletResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class WalletServiceClientFallback implements WalletServiceClient {

    @Override
    public WalletResponseDto getWalletById(Long walletId) {
        log.error("Wallet Service is down. Fallback triggered for getWalletById({})", walletId);
        return createEmptyWalletResponse();
    }

    @Override
    public WalletResponseDto debit(Long walletId, BigDecimal amount) {
        log.error("Wallet Service debit failed. Fallback triggered for walletId: {}, amount: {}", walletId, amount);
        return createEmptyWalletResponse();
    }

    @Override
    public WalletResponseDto credit(Long walletId, BigDecimal amount) {
        log.error("Wallet Service credit failed. Fallback triggered for walletId: {}, amount: {}", walletId, amount);
        return createEmptyWalletResponse();
    }

    @Override
    public WalletResponseDto getWalletByUserId(Long userId) {
        log.error("Wallet Service getWalletByUserId failed for userId: {}", userId);
        return createEmptyWalletResponse();
    }

    @Override
    public boolean acquireLock(Long walletId, long timeoutSeconds) {
        log.error("Wallet Service acquireLock failed for walletId: {}", walletId);
        return false; // Safe fallback - don't acquire lock
    }

    @Override
    public Void releaseLock(Long walletId) {
        log.warn("Wallet Service releaseLock fallback called for walletId: {}", walletId);
        return null;
    }

    private WalletResponseDto createEmptyWalletResponse() {
        WalletResponseDto walletResponseDto = new WalletResponseDto();
        walletResponseDto.setBalance(BigDecimal.ZERO);
        walletResponseDto.setDescription("Service temporarily unavailable");
        return walletResponseDto;
    }


}
