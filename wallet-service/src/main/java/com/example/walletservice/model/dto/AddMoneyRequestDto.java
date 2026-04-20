package com.example.walletservice.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddMoneyRequestDto {

    @NotNull
    @Min(1)
    private BigDecimal amount;
}
