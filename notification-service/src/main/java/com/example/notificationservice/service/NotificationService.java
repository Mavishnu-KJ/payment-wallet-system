package com.example.notificationservice.service;

import com.example.notificationservice.model.dto.NotificationRequestDto;
import com.example.notificationservice.model.dto.NotificationResponseDto;

import java.math.BigDecimal;

public interface NotificationService {

    NotificationResponseDto sendNotification(NotificationRequestDto notificationRequestDto);

    //For internal calls from other services
    void sendAddMoneyNotification(Long userId, BigDecimal amount);
    void sendTransferNotification(Long fromUserId, Long toUserId, BigDecimal amount, String status);

}
