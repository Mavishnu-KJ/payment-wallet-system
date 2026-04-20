package com.example.transactionservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MeTransferRequestDto {

    @NotNull
    private long toWalletId;

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;

}
