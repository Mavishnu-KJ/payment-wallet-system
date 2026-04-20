package com.example.transactionservice.service;

import com.example.transactionservice.model.dto.MeTransferRequestDto;
import com.example.transactionservice.model.dto.TransactionResponseDto;
import com.example.transactionservice.model.dto.TransferRequestDto;

import java.util.List;

public interface TransactionService {

    TransactionResponseDto transfer(TransferRequestDto transferRequestDto);

    List<TransactionResponseDto> getTransactionHistory(Long walletId);

    TransactionResponseDto meTransfer(MeTransferRequestDto meTransferRequestDto);

}
