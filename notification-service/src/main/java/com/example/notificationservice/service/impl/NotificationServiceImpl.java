package com.example.notificationservice.service.impl;

import com.example.notificationservice.model.dto.NotificationRequestDto;
import com.example.notificationservice.model.dto.NotificationResponseDto;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public NotificationResponseDto sendNotification(NotificationRequestDto notificationRequestDto) {
        logger.info("sendNotification, notificationRequestDto is {}", notificationRequestDto);

        simulateSending(notificationRequestDto);

        NotificationResponseDto notificationResponseDto = new NotificationResponseDto();
        notificationResponseDto.setNotificationId("NOTIF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        notificationResponseDto.setUserId(notificationRequestDto.getUserId());
        notificationResponseDto.setType(notificationRequestDto.getType());
        notificationResponseDto.setMessage(notificationRequestDto.getMessage());
        notificationResponseDto.setSentAt(LocalDateTime.now());
        notificationResponseDto.setStatus("SUCCESS");

        logger.info("sendNotification, notificationResponseDto is {}", notificationResponseDto);

        return notificationResponseDto;
    }

    @Override
    public void sendAddMoneyNotification(Long userId, BigDecimal amount) {
        logger.info("sendAddMoneyNotification, userId is {}, amount is {}", userId, amount);

        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setUserId(userId);
        notificationRequestDto.setType("ADD_MONEY");
        notificationRequestDto.setAmount(amount);
        notificationRequestDto.setMessage("₹" + amount + " added to your wallet successfully.");
        logger.info("sendAddMoneyNotification, notificationRequestDto is {}", notificationRequestDto);

        sendNotification(notificationRequestDto);
    }

    @Override
    public void sendTransferNotification(Long fromUserId, Long toUserId, BigDecimal amount, String status) {
        logger.info("sendTransferNotification, fromUserId is {}, toUserId is {}, amount is {}, status is {}",
                fromUserId, toUserId, amount, status);

        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setUserId(fromUserId);
        notificationRequestDto.setType("TRANSFER");
        notificationRequestDto.setAmount(amount);

        if ("SUCCESS".equals(status)) {
            notificationRequestDto.setMessage("₹" + amount + " transferred successfully to user " + toUserId);
        } else {
            notificationRequestDto.setMessage("Transfer of ₹" + amount + " failed.");
        }

        logger.info("sendTransferNotification, notificationRequestDto is {}", notificationRequestDto);

        sendNotification(notificationRequestDto);
    }

    private void simulateSending(NotificationRequestDto request) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📧 NOTIFICATION SENT");
        System.out.println("To User ID     : " + request.getUserId());
        System.out.println("Type           : " + request.getType());
        if (request.getAmount() != null) {
            System.out.println("Amount         : ₹" + request.getAmount());
        }
        System.out.println("Message        : " + request.getMessage());
        System.out.println("Time           : " + LocalDateTime.now());
        System.out.println("=".repeat(70) + "\n");
    }

}
