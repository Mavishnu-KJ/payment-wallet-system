package com.example.transactionservice.service.impl;

import com.example.transactionservice.client.WalletServiceClient;
import com.example.transactionservice.exceptions.InsufficientBalanceException;
import com.example.transactionservice.exceptions.ResourceNotFoundException;
import com.example.transactionservice.model.dto.TransactionResponseDto;
import com.example.transactionservice.model.dto.TransferRequestDto;
import com.example.transactionservice.model.dto.WalletResponseDto;
import com.example.transactionservice.model.entity.Transaction;
import com.example.transactionservice.model.enums.TransactionStatus;
import com.example.transactionservice.model.enums.TransactionType;
import com.example.transactionservice.repository.TransactionRepository;
import com.example.transactionservice.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;
    private final WalletServiceClient walletServiceClient;

    private final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Override
    @Transactional
    public TransactionResponseDto transfer(TransferRequestDto transferRequestDto) {
        logger.info("P2P Transfer started: fromWalletId {} -> toWalletId {} | Amount: {}",
                transferRequestDto.getFromWalletId(), transferRequestDto.getToWalletId(), transferRequestDto.getAmount());

        // Step 1: Validate both wallets exist and are active
        WalletResponseDto fromWallet = walletServiceClient.getWalletById(transferRequestDto.getFromWalletId());
        WalletResponseDto toWallet = walletServiceClient.getWalletById(transferRequestDto.getToWalletId());

        if(fromWallet == null){
            throw new ResourceNotFoundException("Wallet not found for the walletId : "+transferRequestDto.getFromWalletId());
        }

        if(toWallet == null){
            throw new ResourceNotFoundException("Wallet not found for the walletId : "+transferRequestDto.getToWalletId());
        }

        // Step 2: Check sufficient balance
        if (fromWallet.getBalance().compareTo(transferRequestDto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source wallet");
        }

        // Step 3: Create Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setFromWalletId(transferRequestDto.getFromWalletId());
        transaction.setToWalletId(transferRequestDto.getToWalletId());
        transaction.setAmount(transferRequestDto.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(transferRequestDto.getDescription());
        transaction.setTransactionDate(LocalDateTime.now());

        // Step 4: Perform the actual money transfer (Atomic)
        try {
            // Debit from source wallet
            walletServiceClient.debit(transferRequestDto.getFromWalletId(), transferRequestDto.getAmount());

            // Credit to destination wallet
            walletServiceClient.credit(transferRequestDto.getToWalletId(), transferRequestDto.getAmount());

            // Mark transaction as SUCCESS
            transaction.setStatus(TransactionStatus.SUCCESS);

            logger.info("Transfer successful: {}", transaction.getTransactionId());

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            logger.error("Transfer failed for txn: {}", transaction.getTransactionId(), e);
            throw e; // rollback will happen due to @Transactional
        }

        // Step 5: Save transaction record
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("Transfer successful, savedTransaction is {}", savedTransaction);

        return modelMapper.map(savedTransaction, TransactionResponseDto.class);
    }

    @Override
    public List<TransactionResponseDto> getTransactionHistory(Long walletId) {
        logger.info("getTransactionHistory");

        List<Transaction> transactions = transactionRepository
                .findByFromWalletIdOrToWalletIdOrderByTransactionDateDesc(walletId, walletId);
        logger.info("getTransactionHistory, transactions are {}", transactions);

        List<TransactionResponseDto> transactionResponseDtoList = transactions.stream()
                .map(txn -> modelMapper.map(txn, TransactionResponseDto.class))
                .collect(Collectors.toList());

        logger.info("getTransactionHistory, transactionResponseDtoList is {}", transactionResponseDtoList);

        return transactionResponseDtoList;
    }





}
