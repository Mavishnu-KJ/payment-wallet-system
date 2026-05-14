package com.example.walletservice.client;

import com.example.walletservice.model.dto.UserResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)   // Only service name - Eureka will handle discovery
//@CircuitBreaker(name = "userServiceCircuit", fallbackMethod = "getCurrentUserFallback")
public interface UserServiceClient {

    @GetMapping("/api/users/me")
    @CircuitBreaker(name = "userServiceCircuit")
    UserResponseDto getCurrentUser();
    //Returning DTO instead of ResponseEntity is the best practice in feign clients

}
