package com.example.transactionservice.service;

import com.example.transactionservice.model.dto.MeTransferRequestDto;

import java.math.BigDecimal;

public interface SagaService {

    void executeTransferSaga(MeTransferRequestDto meTransferRequestDto, Long fromUserId);

    void compensateTransfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String transactionId, boolean debitSuccess, boolean creditSuccess, boolean transactionSave);
}
