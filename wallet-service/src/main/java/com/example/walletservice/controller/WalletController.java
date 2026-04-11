package com.example.walletservice.controller;

import com.example.walletservice.model.dto.AddMoneyRequestDto;
import com.example.walletservice.model.dto.WalletResponseDto;
import com.example.walletservice.security.CurrentUser;
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
    private final CurrentUser currentUser;

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
    ResponseEntity<Double> getMyWalletBalance(){
        logger.info("getMyWalletBalance");

        //This CurrentUser class we are using for controller endpoint logger only
        String username = currentUser.getCurrentUsername();
        logger.info("getMyWalletBalance, called by username : {}", username);

        Double balance = walletService.getMyWalletBalance();
        logger.info("getMyWalletBalance, balance is {}", balance);

        return ResponseEntity.ok(balance);
    }
}
