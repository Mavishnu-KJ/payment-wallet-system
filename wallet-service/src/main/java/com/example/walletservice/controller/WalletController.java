package com.example.walletservice.controller;

import com.example.walletservice.model.dto.AddMoneyRequestDto;
import com.example.walletservice.model.dto.WalletResponseDto;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@Data
public class WalletController {

    private final WalletService walletService;

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    @PostMapping("/addMoney")
    ResponseEntity<WalletResponseDto> addMoney(@Valid @RequestBody AddMoneyRequestDto addMoneyRequestDto){
        logger.info("addMoney, addMoneyRequestDto is {}", addMoneyRequestDto);

        WalletResponseDto walletResponseDto = walletService.addMoney(addMoneyRequestDto);
        logger.info("addMoney, walletResponseDto is {}", walletResponseDto);

        return ResponseEntity.ok(walletResponseDto);
    }

    @GetMapping("/{userId}")
    ResponseEntity<WalletResponseDto> getWalletByUserId(@PathVariable(name = "userId") Long userId){
        logger.info("getWalletByUserId, userId is {}", userId);

        WalletResponseDto walletResponseDto = walletService.getWalletByUserId(userId);
        logger.info("getWalletByUserId, walletResponseDto is {}", walletResponseDto);

        return ResponseEntity.ok(walletResponseDto);
    }

    @GetMapping("/{userId}/balance")
    ResponseEntity<Double> getBalanceByUserId(@PathVariable(name = "userId") Long userId){
        logger.info("getBalanceByUserId, userId is {}", userId);

        Double balance = walletService.getBalanceByUserId(userId);
        logger.info("getBalanceByUserId, balance is {}", balance);

        return ResponseEntity.ok(balance);
    }
}
