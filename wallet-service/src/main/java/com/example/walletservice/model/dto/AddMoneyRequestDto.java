package com.example.walletservice.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMoneyRequestDto {

    @NotNull
    private Long userId;

    @NotNull
    @Min(1)
    private Double amount;
}
