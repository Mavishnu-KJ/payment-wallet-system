package com.example.apigateway;


import com.example.apigateway.filter.RateLimitingFilter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RateLimitingFilter rateLimitingFilter(RateLimiterRegistry rateLimiterRegistry) {
        return new RateLimitingFilter(rateLimiterRegistry);
    }
}
