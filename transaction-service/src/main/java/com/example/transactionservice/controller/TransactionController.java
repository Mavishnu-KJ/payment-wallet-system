package com.example.transactionservice.controller;

import com.example.transactionservice.model.dto.MeTransferRequestDto;
import com.example.transactionservice.model.dto.TransactionResponseDto;
import com.example.transactionservice.model.dto.TransferRequestDto;
import com.example.transactionservice.security.CurrentUser;
import com.example.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentUser currentUser;

    private final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @PostMapping("/transfer")
    ResponseEntity<TransactionResponseDto> transfer(@Valid @RequestBody TransferRequestDto transferRequestDto){
        logger.info("transfer, transferRequestDto is {}", transferRequestDto);

        //This CurrentUser class we are using for controller endpoint logger only
        String username = currentUser.getCurrentUsername();
        logger.info("transfer, called by username : {}", username);

        TransactionResponseDto transactionResponseDto = transactionService.transfer(transferRequestDto);
        logger.info("transfer, transactionResponseDto is {}", transactionResponseDto);

        return ResponseEntity.ok(transactionResponseDto);
    }

    @GetMapping("/history/{walletId}")
    ResponseEntity<List<TransactionResponseDto>> getTransactionHistory(@PathVariable @Valid Long walletId){
        logger.info("getTransactionHistory, walletId is {}", walletId);

        //This CurrentUser class we are using for controller endpoint logger only
        String username = currentUser.getCurrentUsername();
        logger.info("getTransactionHistory, called by username : {}", username);

        List<TransactionResponseDto> transactionResponseDtoList = transactionService.getTransactionHistory(walletId);
        logger.info("getTransactionHistory, transactionResponseDtoList is {}", transactionResponseDtoList);

        return ResponseEntity.ok(transactionResponseDtoList);
    }

    @PostMapping("/me/transfer")
    ResponseEntity<TransactionResponseDto> meTransfer(@Valid @RequestBody MeTransferRequestDto meTransferRequestDto){
        logger.info("meTransfer, meTransferRequestDto is {}", meTransferRequestDto);

        //This CurrentUser class we are using for controller endpoint logger only
        String username = currentUser.getCurrentUsername();
        logger.info("meTransfer, called by username : {}", username);

        TransactionResponseDto transactionResponseDto = transactionService.meTransfer(meTransferRequestDto);
        logger.info("meTransfer, transactionResponseDto is {}", transactionResponseDto);

        return ResponseEntity.ok(transactionResponseDto);
    }

}
