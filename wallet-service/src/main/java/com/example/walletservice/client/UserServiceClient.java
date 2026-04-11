package com.example.walletservice.client;

import com.example.walletservice.model.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//url = "${user-service.url:http://localhost:8081}" - default value for user-service.url
@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
public interface UserServiceClient {

    @GetMapping("/api/users/me")
    UserResponseDto getCurrentUser();
    //Returning DTO instead of ResponseEntity is the best practice in feign clients

}
