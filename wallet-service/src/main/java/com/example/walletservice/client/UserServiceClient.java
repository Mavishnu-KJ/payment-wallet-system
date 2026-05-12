package com.example.walletservice.client;

import com.example.walletservice.model.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")   // Only service name - Eureka will handle discovery
public interface UserServiceClient {

    @GetMapping("/api/users/me")
    UserResponseDto getCurrentUser();
    //Returning DTO instead of ResponseEntity is the best practice in feign clients

}
