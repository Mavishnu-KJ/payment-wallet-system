package com.example.transactionservice.service.impl;

import com.example.transactionservice.client.NotificationServiceClient;
import com.example.transactionservice.client.UserServiceClient;
import com.example.transactionservice.client.WalletServiceClient;
import com.example.transactionservice.exceptions.InsufficientBalanceException;
import com.example.transactionservice.exceptions.ResourceConflictException;
import com.example.transactionservice.exceptions.ResourceNotFoundException;
import com.example.transactionservice.model.dto.*;
import com.example.transactionservice.model.entity.Transaction;
import com.example.transactionservice.model.enums.*;
import com.example.transactionservice.repository.TransactionRepository;
import com.example.transactionservice.security.CurrentUser;
import com.example.transactionservice.service.SagaService;
import com.example.transactionservice.service.TransactionService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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
    private final NotificationServiceClient notificationServiceClient;
    private final SagaService sagaService;
    private final CurrentUser currentUser;

    private final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);

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
    @CircuitBreaker(name = "meTransferCircuit", fallbackMethod = "meTransferFallback")
    @Retry(name = "meTransferRetry")
    public TransactionResponseDto meTransfer(MeTransferRequestDto meTransferRequestDto){
        logger.info("P2P meTransfer, meTransferRequestDto is {}", meTransferRequestDto);

        //JWT filter at Gateway level for centralized auth - start
        Long loggedInUserId = currentUser.getCurrentUserId();
        logger.info("P2P meTransfer, UserId from header is {}", loggedInUserId);

        if(loggedInUserId == null){
            //Call user-service via Feign (for direct calls on port 8083)
            logger.info("P2P meTransfer, UserId not found in header. Falling back to Feign call");
            UserResponseDto userResponseDto = userServiceClient.getCurrentUser();
            logger.info("P2P meTransfer, userResponseDto is {}", userResponseDto);

            //Check for whether the userId exists or not in user-service
            if(userResponseDto == null || userResponseDto.getUserStatus() != UserStatus.ACTIVE){
                throw new ResourceNotFoundException("User not found or not active");
            }

            loggedInUserId = userResponseDto.getUserId();
            logger.info("P2P meTransfer, loggedInUserId is {}", loggedInUserId);
        }
        //JWT filter at Gateway level for centralized auth - end

        logger.info("P2P meTransfer, transfer initiated by loggedInUserId is {}", loggedInUserId);

        //Get the user's own wallet
        WalletResponseDto fromWallet = walletServiceClient.getWalletByUserId(loggedInUserId);
        logger.info("P2P meTransfer, fromWallet is {}", fromWallet);

        //Validate destination wallet
        WalletResponseDto toWallet = walletServiceClient.getWalletById(meTransferRequestDto.getToWalletId());
        logger.info("P2P meTransfer, toWallet is {}", toWallet);

        //Validate both wallets exist and is active
        if(fromWallet == null){
            throw new ResourceNotFoundException("Your source wallet not found");
        }

        if (fromWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Your source wallet is not active, walletId : "+fromWallet.getId());
        }

        if(toWallet == null){
            throw new ResourceNotFoundException("Destination wallet not found, walletId : "+meTransferRequestDto.getToWalletId());
        }

        if (toWallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Destination wallet is not active, walletId : "+meTransferRequestDto.getToWalletId());
        }

        //Prevent self-transfer
        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new IllegalArgumentException("Cannot transfer money to your own wallet");
        }

        //Check sufficient balance
        if (fromWallet.getBalance().compareTo(meTransferRequestDto.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source wallet");
        }

        //DistributedLocking
        Long fromWalletId = fromWallet.getId();
        boolean lockAcquired = walletServiceClient.acquireLock(fromWalletId, 60); // 60 seconds timeout
        if (!lockAcquired) {
            throw new ResourceConflictException("Due to DistributedLocking, another operation is in progress on this wallet. Please try again later.");
        }
        logger.info("P2P meTransfer, DistributedLocking lock acquired for fromWalletId : {}, amount : {}", fromWalletId, meTransferRequestDto.getAmount());

        try {
            //Call Saga Service (Main Logic)
            sagaService.executeTransferSaga(meTransferRequestDto, loggedInUserId);
            logger.info("P2P meTransfer, Saga transfer logic is executed");

            Transaction lastSavedTransaction = transactionRepository.findTopByFromWalletIdOrderByTransactionDateDesc(fromWalletId);
            logger.info("P2P meTransfer, lastSavedTransaction is {}", lastSavedTransaction);

            if (lastSavedTransaction == null) {
                throw new ResourceNotFoundException("Transaction not found after processing");
            }

            logger.info("P2P meTransfer, Transfer completed successfully, transactionId is {}", lastSavedTransaction.getTransactionId());

            return modelMapper.map(lastSavedTransaction, TransactionResponseDto.class);

        } catch (Exception e){
            logger.error("P2P meTransfer, Transfer failed for wallet: {}. Error: {}", fromWalletId, e.getMessage(), e);
            throw e; //Let global exception handler manage the response
        } finally {
            // CRITICAL: Always release the lock
            try {
                walletServiceClient.releaseLock(fromWalletId);
                logger.info("P2P meTransfer, DistributedLocking Lock released for wallet: {}", fromWalletId);
            } catch (Exception lockEx) {
                logger.warn("P2P meTransfer, DistributedLocking Failed to release lock for wallet: {}. Error: {}", fromWalletId, lockEx.getMessage());
                // Do not throw here - we don't want to mask original exception
            }
        }

    }

    public Page<TransactionResponseDto> getMyTransactionHistory(int page, int size){
        logger.info("getMyTransactionHistory, page is {}, size is {}", page, size);

        //JWT filter at Gateway level for centralized auth - start
        Long loggedInUserId = currentUser.getCurrentUserId();
        logger.info("getMyTransactionHistory, UserId from header is {}", loggedInUserId);

        if(loggedInUserId == null){
            //Call user-service via Feign (for direct calls on port 8083)
            logger.info("getMyTransactionHistory, UserId not found in header. Falling back to Feign call");
            UserResponseDto userResponseDto = userServiceClient.getCurrentUser();
            logger.info("getMyTransactionHistory, userResponseDto is {}", userResponseDto);

            //Check for whether the userId exists or not in user-service
            if(userResponseDto == null || userResponseDto.getUserStatus() != UserStatus.ACTIVE){
                throw new ResourceNotFoundException("User not found or not active");
            }

            loggedInUserId = userResponseDto.getUserId();
            logger.info("getMyTransactionHistory, loggedInUserId is {}", loggedInUserId);
        }
        //JWT filter at Gateway level for centralized auth - end

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

    //Fallback method for meTransfer
    public TransactionResponseDto meTransferFallback(MeTransferRequestDto meTransferRequestDto, Throwable throwable) {
        logger.error("meTransferFallback, meTransfer failed. Circuit Breaker opened or retry exhausted. Request: {}",
                meTransferRequestDto, throwable);

        // Safe fallback response
        TransactionResponseDto fallback = new TransactionResponseDto();
        fallback.setDescription("Transfer service is temporarily unavailable. Please try again later.");
        fallback.setStatus(TransactionStatus.FAILED);
        fallback.setAmount(meTransferRequestDto.getAmount());

        return fallback;
    }

}
