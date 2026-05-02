package com.example.notificationservice.model.dto;

import java.time.LocalDateTime;

public class NotificationResponseDto {
    private String notificationId;
    private Long userId;
    private String type;
    private String message;
    private LocalDateTime sentAt;
    private String status;                      // SUCCESS, FAILED
}
