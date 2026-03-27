package com.example.walletservice.client;

import com.example.walletservice.model.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://localhost:8081")
public interface UserServiceClient {

    @GetMapping("/api/users/{userId}")
    UserResponseDto getUserById(@PathVariable(name = "userId") Long id);
    //Returning DTO instead of ResponseEntity is the best practice in feign clients

}
