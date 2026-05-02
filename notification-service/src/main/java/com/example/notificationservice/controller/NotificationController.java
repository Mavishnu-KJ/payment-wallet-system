package com.example.notificationservice.controller;

import com.example.notificationservice.model.dto.NotificationRequestDto;
import com.example.notificationservice.model.dto.NotificationResponseDto;
import com.example.notificationservice.security.CurrentUser;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private NotificationService notificationService;
    private CurrentUser currentUser;

    private final static Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping
    public ResponseEntity<NotificationResponseDto> sendNotification(
            @RequestBody NotificationRequestDto notificationRequestDto) {

        logger.info("sendNotification, NotificationRequestDto is {}", notificationRequestDto);

        String username = currentUser.getCurrentUsername();
        logger.info("sendNotification, called by username : {}", username);

        NotificationResponseDto notificationResponseDto = notificationService.sendNotification(notificationRequestDto);
        logger.info("sendNotification, notificationResponseDto is {}", notificationResponseDto);

        return ResponseEntity.ok(notificationResponseDto);
    }

    // === Internal endpoints for other services (like Transaction Service) ===
    @PostMapping("/internal/add-money")
    public ResponseEntity<Void> notifyAddMoney(@RequestParam Long userId, @RequestParam BigDecimal amount) {
        logger.info("Internal call: notifyAddMoney for userId: {}, amount: {}", userId, amount);

        notificationService.sendAddMoneyNotification(userId, amount);
        logger.info("Internal call: sendAddMoneyNotification completed for userId: {}, amount: {}", userId, amount);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/internal/transfer")
    public ResponseEntity<Void> notifyTransfer(
            @RequestParam Long fromUserId,
            @RequestParam Long toUserId,
            @RequestParam BigDecimal amount,
            @RequestParam String status) {

        logger.info("Internal: notifyTransfer, fromUserId: {}, toUserId: {}, amount: {}, status: {}",
                fromUserId, toUserId, amount, status);

        notificationService.sendTransferNotification(fromUserId, toUserId, amount, status);
        logger.info("Internal call: sendTransferNotification completed, fromUserId: {}, toUserId: {}, amount: {}, status: {}",
                fromUserId, toUserId, amount, status);

        return ResponseEntity.ok().build();
    }

}
