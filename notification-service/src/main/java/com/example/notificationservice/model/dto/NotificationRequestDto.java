package com.example.notificationservice.model.dto;

import lombok.Data;

@Data
public class NotificationRequestDto {
    private Long userId;
    private String type;            // ADD_MONEY, TRANSFER, LOW_BALANCE, etc.
    private String message;
    private String recipientEmail;
}