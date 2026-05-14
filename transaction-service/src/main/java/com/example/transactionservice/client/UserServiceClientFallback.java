package com.example.transactionservice.client;

import com.example.transactionservice.model.dto.UserResponseDto;
import com.example.transactionservice.model.enums.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserResponseDto getCurrentUser() {
        log.error("getCurrentUser, User Service is down or slow. Returning fallback response.");

        UserResponseDto fallback = new UserResponseDto();
        fallback.setUserId(0L);
        fallback.setUsername("fallback-user");
        fallback.setUserStatus(UserStatus.INACTIVE);
        fallback.setEmail("fallback@example.com");
        fallback.setFullName("System Fallback");

        return fallback;
    }

}
