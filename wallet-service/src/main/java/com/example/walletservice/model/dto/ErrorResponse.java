package com.example.walletservice.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int statusCode,
        String shortSummaryMessage,
        List<String> errorList,
        LocalDateTime timestamp) {
}