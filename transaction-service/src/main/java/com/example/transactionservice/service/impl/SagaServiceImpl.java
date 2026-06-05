package com.example.transactionservice.service.impl;

import com.example.transactionservice.client.NotificationServiceClient;
import com.example.transactionservice.client.WalletServiceClient;
import com.example.transactionservice.exceptions.ResourceNotFoundException;
import com.example.transactionservice.model.dto.MeTransferRequestDto;
import com.example.transactionservice.model.dto.WalletResponseDto;
import com.example.transactionservice.model.entity.Transaction;
import com.example.transactionservice.model.enums.TransactionStatus;
import com.example.transactionservice.model.enums.TransactionType;
import com.example.transactionservice.model.enums.WalletStatus;
import com.example.transactionservice.repository.TransactionRepository;
import com.example.transactionservice.service.SagaService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaServiceImpl implements SagaService {

    private final WalletServiceClient walletServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final TransactionRepository transactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher; //Spring provides ApplicationEventPublisher

    private static final Logger logger = LoggerFactory.getLogger(SagaServiceImpl.class);

    @Override
    @Transactional
    @CircuitBreaker(name = "meTransferCircuit", fallbackMethod = "meTransferFallback")
    @Retry(name = "meTransferRetry")
    public void executeTransferSaga(MeTransferRequestDto meTransferRequestDto, Long fromUserId) {
        logger.info("SAGA : executeTransferSaga, meTransferRequestDto is {} and fromUserId is {}", meTransferRequestDto, fromUserId);

        WalletResponseDto fromWallet = walletServiceClient.getWalletByUserId(fromUserId);
        logger.info("SAGA : executeTransferSaga, fromWallet is {}", fromWallet);

        WalletResponseDto toWallet = walletServiceClient.getWalletById(meTransferRequestDto.getToWalletId());
        logger.info("SAGA : executeTransferSaga, toWallet is {}", toWallet);

        if(fromWallet == null || fromWallet.getStatus() != WalletStatus.ACTIVE){
            throw new ResourceNotFoundException("Your source wallet not found or inactive");
        }

        if(toWallet == null || toWallet.getStatus() != WalletStatus.ACTIVE){
            throw new ResourceNotFoundException("Destination wallet not found or inactive");
        }

        long fromWalletId = fromWallet.getId();
        long toWalletId = toWallet.getId();
        BigDecimal amount = meTransferRequestDto.getAmount();

        //Generate unique transaction reference
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        logger.info("SAGA : executeTransferSaga, transactionId is {}", transactionId);

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setFromWalletId(fromWalletId);
        transaction.setToWalletId(toWalletId);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(meTransferRequestDto.getDescription());
        transaction.setTransactionDate(LocalDateTime.now());

        boolean debitSuccess = false;
        boolean creditSuccess = false;
        boolean transactionSave = false;

        try {
            logger.info("SAGA : executeTransferSaga, Saga started, transaction is {}", transaction);

            //Step 1: Debit
            walletServiceClient.debit(fromWalletId, amount);
            debitSuccess = true;
            logger.info("SAGA: executeTransferSaga, Debit successful for transaction {}", transactionId);

            //Step 2: Credit
            walletServiceClient.credit(toWalletId, amount);
            creditSuccess = true;
            logger.info("SAGA: executeTransferSaga, Credit successful for transaction {}", transactionId);

            //Step 3: Mark Success
            transaction.setStatus(TransactionStatus.SUCCESS);

            //Step 4 : Save transaction record
            transactionRepository.save(transaction);
            transactionSave = true;
            logger.info("SAGA : executeTransferSaga, Saga transaction saved successfully, transaction is {}", transaction);

            //Send notification : SUCCESS
            notificationServiceClient.notifyTransfer(
                    fromWallet.getUserId(),
                    toWallet.getUserId(),
                    amount,
                    "SUCCESS"
            );

            logger.info("SAGA : executeTransferSaga, Saga completed successfully, transactionId is {}", transactionId);


        } catch (Exception e) {
            logger.error("SAGA : executeTransferSaga, Saga failed for transaction: {}. Starting compensation...", transactionId, e);

            //Saga Compensation
            compensateTransfer(fromWalletId, toWalletId, amount, transactionId, debitSuccess, creditSuccess, transactionSave);

            transaction.setStatus(TransactionStatus.FAILED);

            //Still save failed transaction (optional but recommended)
            // Only save if critical fields are set
            if (transaction.getTransactionId() != null && transaction.getAmount() != null) {
                transactionRepository.save(transaction);
            }

            //Send notification : FAILED
            notificationServiceClient.notifyTransfer(
                    fromWallet.getUserId(),
                    toWallet.getUserId(),
                    amount,
                    "FAILED"
            );

            throw new RuntimeException("executeTransferSaga, Transfer saga failed and compensated", e);
        }
    }

    @Override
    public void compensateTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String transactionId, boolean debitSuccess, boolean creditSuccess, boolean transactionSave) {

        logger.info("SAGA : compensateTransfer, fromWalletId is {} and toWalletId is {}, amount is {}, transactionId is {}, debitSuccess is {}, creditSuccess is {}, transactionSave is {}",
                fromWalletId, toWalletId, amount, transactionId, debitSuccess, creditSuccess, transactionSave);

        try {
            // Case 1: Debit succeeded but Credit failed → Refund sender
            if (debitSuccess && !creditSuccess) {
                // Only refund if debit happened but credit failed
                logger.info("SAGA: compensateTransfer, Compensating - Refunding sender for txn {}", transactionId);
                walletServiceClient.credit(fromWalletId, amount);   // Refund
                logger.info("SAGA: compensateTransfer, Compensation (refund) completed for txn {}", transactionId);
            }
            // Case 2: Both Debit and Credit succeeded but Transaction Save failed
            else if (debitSuccess && creditSuccess && !transactionSave) {
                logger.warn("SAGA : compensateTransfer, Debit & Credit succeeded but transaction save failed for txn {}. No refund needed.", transactionId);
                // Optional: We can retry saving the transaction here
                // transactionRepository.save(...) with retry logic
            }
            // Case 3: No money was moved (failure happened before debit or after full success)
            else {
                logger.info("SAGA : compensateTransfer, No compensation required for txn {}", transactionId);
            }
        } catch (Exception ex) {
            logger.error("SAGA : compensateTransfer, Compensation also failed! Manual intervention needed for txn: {}", transactionId);
            //In real production, we would send alert to operations team
        }
    }

    public void meTransferFallback(MeTransferRequestDto request, Throwable throwable) {
        logger.error("meTransferFallback, meTransferSaga failed. Circuit Breaker opened or retry exhausted. Request: {}", request, throwable);

        // Minimal safe actions only (no dependency on missing fields)
        logger.warn("meTransferFallback, Transfer operation failed for user. Please try again later.");
    }

}
