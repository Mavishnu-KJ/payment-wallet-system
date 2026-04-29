package com.example.walletservice.controller;

import com.example.walletservice.model.dto.AddMoneyRequestDto;
import com.example.walletservice.model.dto.WalletResponseDto;
import com.example.walletservice.security.CurrentUser;
import com.example.walletservice.service.RedisLockService;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final CurrentUser currentUser;
    private final RedisLockService redisLockService;

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    ///me endpoints are secured

    @PostMapping("/me/addMoney")
    ResponseEntity<WalletResponseDto> addMoney(@Valid @RequestBody AddMoneyRequestDto addMoneyRequestDto){
        logger.info("addMoney, addMoneyRequestDto is {}", addMoneyRequestDto);

        //This CurrentUser class we are using for controller endpoint logger only
        String username = currentUser.getCurrentUsername();
        logger.info("addMoney, called by username : {}", username);

        WalletResponseDto walletResponseDto = walletService.addMoney(addMoneyRequestDto);
        logger.info("addMoney, walletResponseDto is {}", walletResponseDto);

        return ResponseEntity.ok(walletResponseDto);
    }

    @GetMapping("/me")
    ResponseEntity<WalletResponseDto> getMyWallet(){
        logger.info("getMyWallet");

        //This CurrentUser class we are using for controller endpoint logger only
        String username = currentUser.getCurrentUsername();
        logger.info("getMyWallet, called by username : {}", username);

        WalletResponseDto walletResponseDto = walletService.getMyWallet();
        logger.info("getWalletByUserId, walletResponseDto is {}", walletResponseDto);

        return ResponseEntity.ok(walletResponseDto);
    }

    @GetMapping("/me/balance")
    ResponseEntity<BigDecimal> getMyWalletBalance(){
        logger.info("getMyWalletBalance");

        //This CurrentUser class we are using for controller endpoint logger only
        String username = currentUser.getCurrentUsername();
        logger.info("getMyWalletBalance, called by username : {}", username);

        BigDecimal balance = walletService.getMyWalletBalance();
        logger.info("getMyWalletBalance, balance is {}", balance);

        return ResponseEntity.ok(balance);
    }

    // === Internal endpoints for other services (like Transaction Service) ===
    @GetMapping("/internal/{walletId}")
    ResponseEntity<WalletResponseDto> getWalletById(@PathVariable Long walletId) {
        logger.info("Internal call: getWalletById for walletId: {}", walletId);

        WalletResponseDto walletResponseDto = walletService.getWalletById(walletId);
        logger.info("Internal call: getWalletById, walletResponseDto is {}", walletResponseDto);

        return ResponseEntity.ok(walletResponseDto);
    }

    @PostMapping("/internal/{walletId}/debit")
    ResponseEntity<WalletResponseDto> debit(@PathVariable Long walletId, @RequestBody BigDecimal amount) {
        logger.info("Internal call: debit, walletId: {}, amount: {}", walletId, amount);

        WalletResponseDto walletResponseDto = walletService.debit(walletId, amount);
        logger.info("Internal call: debit, walletResponseDto is {}", walletResponseDto);

        return ResponseEntity.ok(walletResponseDto);
    }

    @PostMapping("/internal/{walletId}/credit")
    ResponseEntity<WalletResponseDto> credit(@PathVariable Long walletId, @RequestBody BigDecimal amount) {
        logger.info("Internal call: credit, walletId: {}, amount: {}", walletId, amount);

        WalletResponseDto walletResponseDto = walletService.credit(walletId, amount);
        logger.info("Internal call: credit, walletResponseDto is {}", walletResponseDto);

        return ResponseEntity.ok(walletResponseDto);
    }

    @GetMapping("/internal/user/{userId}")
    ResponseEntity<WalletResponseDto> getWalletByUserId(@PathVariable("userId") Long userId){
        logger.info("Internal call: getWalletByUserId, userId: {}", userId);

        WalletResponseDto walletResponseDto = walletService.getWalletByUserId(userId);
        logger.info("Internal call: getWalletByUserId, walletResponseDto : {}", walletResponseDto);

        return ResponseEntity.ok(walletResponseDto);

    }

    //Methods related to Redis, Distributed locking
    @PostMapping("/internal/{walletId}/acquireLock")
    ResponseEntity<Boolean> acquireLock(@PathVariable Long walletId, @RequestParam(defaultValue = "60") long timeoutSeconds) {
        logger.info("Internal call: acquireLockWithTimeout, walletId: {}, timeoutSeconds: {}", walletId, timeoutSeconds);

        boolean lockAcquired = redisLockService.acquireLockWithTimeout(walletId, timeoutSeconds);
        logger.info("Internal call: acquireLockWithTimeout, lockAcquired is {}", lockAcquired);

        return ResponseEntity.ok(lockAcquired);
    }

    @PostMapping("/internal/{walletId}/releaseLock")
    ResponseEntity<Void> releaseLock(@PathVariable Long walletId) {
        logger.info("Internal call: releaseLock, walletId: {}", walletId);

        redisLockService.releaseLock(walletId);
        logger.info("Internal call: releaseLock completed for walletId: {}", walletId);

        return ResponseEntity.ok().build();         // 200 OK with no body
    }


}
