package com.example.transactionservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

//@FeignClient(name = "notification-service", url = "${notification-service.url:http://localhost:8084}")
@FeignClient(name = "notification-service", url = "http://notification-service:8080") //Changed url because of docker
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/internal/add-money")
    void notifyAddMoney(
            @RequestParam("userId") Long userId,
            @RequestParam("amount") BigDecimal amount
    );

    @PostMapping("/api/notifications/internal/transfer")
    void notifyTransfer(
            @RequestParam("fromUserId") Long fromUserId,
            @RequestParam("toUserId") Long toUserId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("status") String status
    );

}
