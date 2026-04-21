package com.example.transactionservice.service.impl;

import com.example.transactionservice.client.UserServiceClient;
import com.example.transactionservice.client.WalletServiceClient;
import com.example.transactionservice.exceptions.InsufficientBalanceException;
import com.example.transactionservice.exceptions.ResourceNotFoundException;
import com.example.transactionservice.model.dto.*;
import com.example.transactionservice.model.entity.Transaction;
import com.example.transactionservice.model.enums.*;
import com.example.transactionservice.repository.TransactionRepository;
import com.example.transactionservice.security.CurrentUser;
import com.example.transactionservice.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final UserServiceClient userServiceClient;

    private final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Override
    @Transactional
    public TransactionResponseDto transfer(TransferRequestDto transferRequestDto) {
        logger.info("P2P Transfer started: fromWalletId {} -> toWalletId {} | Amount: {}",
                transferRequestDto.getFromWalletId(), transferRequestDto.getToWalletId(), transferRequestDto.getAmount());

        // Step 1: Get current logged-in user from User Service
        UserResponseDto currentUserResponseDto = userServiceClient.getCurrentUser();
        logger.info("P2P Transfer, currentUserResponseDto is {}", currentUserResponseDto);

        if(currentUserResponseDto == null || currentUserResponseDto.getUserStatus() != UserStatus.ACTIVE){
            throw new ResourceNotFoundException("User not found or inactive");
        }

        Long loggedInUserId = currentUserResponseDto.getUserId();
        logger.info("P2P Transfer, transfer initiated by loggedInUserId is {}", loggedInUserId);

        // Step 2: Validate both wallets exist and are active
        WalletResponseDto fromWallet = walletServiceClient.getWalletById(transferRequestDto.getFromWalletId());
        WalletResponseDto toWallet = walletServiceClient.getWalletById(transferRequestDto.getToWalletId());

        logger.info("P2P Transfer, fromWallet is {}", fromWallet);
        logger.info("P2P Transfer, toWallet is {}", toWallet);

        if(fromWallet == null){
            throw new ResourceNotFoundException("Wallet not found for the walletId : "+transferRequestDto.getFromWalletId());
        }

        if(toWallet == null){
            throw new ResourceNotFoundException("Wallet not found for the walletId : "+transferRequestDto.getToWalletId());
        }

        if (fromWallet.getStatus() != WalletStatus.ACTIVE || toWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("One or both wallets are not active");
        }

        // Step 3: Ownership Validation (Security Check)
        if (!fromWallet.getUserId().equals(loggedInUserId)) {
            throw new IllegalArgumentException("You can only transfer money from your own wallet");
        }

        // Step 4: Check sufficient balance
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

    @Override
    @Transactional
    public TransactionResponseDto meTransfer(MeTransferRequestDto meTransferRequestDto){
        logger.info("P2P meTransfer started: toWalletId {} | Amount: {}",
                meTransferRequestDto.getToWalletId(), meTransferRequestDto.getAmount());

        // Step 1: Get current logged-in user from User Service
        UserResponseDto currentUserResponseDto = userServiceClient.getCurrentUser();
        logger.info("P2P meTransfer, currentUserResponseDto is {}", currentUserResponseDto);

        if(currentUserResponseDto == null || currentUserResponseDto.getUserStatus() != UserStatus.ACTIVE){
            throw new ResourceNotFoundException("User not found or inactive");
        }

        Long loggedInUserId = currentUserResponseDto.getUserId();
        logger.info("P2P meTransfer, transfer initiated by loggedInUserId is {}", loggedInUserId);

        // Step 2: Get the user's own wallet
        WalletResponseDto fromWallet = walletServiceClient.getWalletByUserId(loggedInUserId);
        logger.info("P2P meTransfer, fromWallet is {}", fromWallet);

        // Step 3: Validate both wallets exist and is active
        WalletResponseDto toWallet = walletServiceClient.getWalletById(meTransferRequestDto.getToWalletId());
        logger.info("P2P meTransfer, toWallet is {}", toWallet);

        if(fromWallet == null){
            throw new ResourceNotFoundException("Your source wallet not found");
        }

        if(toWallet == null){
            throw new ResourceNotFoundException("Destination wallet not found, walletId : "+meTransferRequestDto.getToWalletId());
        }

        if (fromWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Your source wallet is not active, walletId : "+fromWallet.getId());
        }

        if (toWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Destination wallet is not active, walletId : "+meTransferRequestDto.getToWalletId());
        }

        // Step 4: Prevent self-transfer
        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new IllegalArgumentException("Cannot transfer money to your own wallet");
        }

        // Step 5: Check sufficient balance
        if (fromWallet.getBalance().compareTo(meTransferRequestDto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source wallet");
        }

        // Step 6: Create Transaction record (PENDING)
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transaction.setFromWalletId(fromWallet.getId());
        transaction.setToWalletId(meTransferRequestDto.getToWalletId());
        transaction.setAmount(meTransferRequestDto.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(meTransferRequestDto.getDescription());
        transaction.setTransactionDate(LocalDateTime.now());

        // Step 7: Perform the actual money transfer (Atomic)
        try {

            //Perform atomic transfer
            // Debit from source wallet
            walletServiceClient.debit(fromWallet.getId(), meTransferRequestDto.getAmount());

            // Credit to destination wallet
            walletServiceClient.credit(meTransferRequestDto.getToWalletId(), meTransferRequestDto.getAmount());

            // Mark transaction as SUCCESS
            transaction.setStatus(TransactionStatus.SUCCESS);

            logger.info("meTransfer successful: {}", transaction.getTransactionId());

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            logger.error("meTransfer failed for txn: {}", transaction.getTransactionId(), e);
            throw e; // rollback will happen due to @Transactional
        }

        // Step 8: Save transaction record
        Transaction savedTransaction = transactionRepository.save(transaction);
        logger.info("meTransfer successful, savedTransaction is {}", savedTransaction);

        return modelMapper.map(savedTransaction, TransactionResponseDto.class);

    }

    public Page<TransactionResponseDto> getMyTransactionHistory(int page, int size){
        logger.info("getMyTransactionHistory, page is {}, size is {}", page, size);

        //Get current logged-in user from User Service
        UserResponseDto currentUserResponseDto = userServiceClient.getCurrentUser();
        logger.info("getMyTransactionHistory, currentUserResponseDto is {}", currentUserResponseDto);

        if(currentUserResponseDto == null || currentUserResponseDto.getUserStatus() != UserStatus.ACTIVE){
            throw new ResourceNotFoundException("User not found or inactive");
        }

        Long loggedInUserId = currentUserResponseDto.getUserId();
        logger.info("getMyTransactionHistory, loggedInUserId is {}", loggedInUserId);

        //Get user's wallet
        WalletResponseDto wallet = walletServiceClient.getWalletByUserId(loggedInUserId);

        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for logged-in user, loggedInUserId : "+loggedInUserId);
        }

        Long userWalletId = wallet.getId();
        logger.info("getMyTransactionHistory, userWalletId is {}", userWalletId);

        // Fetch paginated history
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());

        Page<Transaction> transactionPage = transactionRepository
                .findByFromWalletIdOrToWalletId(pageable, userWalletId, userWalletId);

        logger.info("getMyTransactionHistory, page size : {}, walletId : {}, page : {}",
                transactionPage.getContent().size(), userWalletId, page);

        Page<TransactionResponseDto> transactionResponseDtoPage = transactionPage
                .map(txn -> {
                   TransactionResponseDto transactionResponseDto = modelMapper.map(txn, TransactionResponseDto.class);

                    // From user's perspective
                    if (txn.getFromWalletId().equals(userWalletId)) {
                        transactionResponseDto.setTransactionDisplayType(TransactionDisplayType.DEBIT);
                    } else {
                        transactionResponseDto.setTransactionDisplayType(TransactionDisplayType.CREDIT);
                    }

                   return transactionResponseDto;
                });

        logger.info("getMyTransactionHistory, transactionResponseDtoPage is {}", transactionResponseDtoPage);

        return transactionResponseDtoPage;
    }



}
