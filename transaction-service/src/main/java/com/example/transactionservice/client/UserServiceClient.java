package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

//@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
//@FeignClient(name = "user-service", url = "http://user-service:8080") //Changed url because of docker
@FeignClient(name = "user-service") //Just service name enough, Eureka service discovery wil tc
public interface UserServiceClient {

    @GetMapping("/api/users/me")
    UserResponseDto getCurrentUser();

}
