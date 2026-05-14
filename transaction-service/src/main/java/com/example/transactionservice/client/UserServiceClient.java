package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.UserResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

//@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8081}")
//@FeignClient(name = "user-service", url = "http://user-service:8080") //Changed url because of docker
//@FeignClient(name = "user-service") //Just service name enough, Eureka service discovery wil tc
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/users/me")
    @CircuitBreaker(name = "userServiceCircuit")
    UserResponseDto getCurrentUser();

}
