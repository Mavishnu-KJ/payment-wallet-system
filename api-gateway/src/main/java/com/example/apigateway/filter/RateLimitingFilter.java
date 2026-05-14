package com.example.apigateway.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RateLimiterRegistry rateLimiterRegistry;
    private final Map<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();

    public RateLimitingFilter(RateLimiterRegistry rateLimiterRegistry) {
        super(Config.class);
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String userId = getUserIdFromRequest(exchange);   // Extract from JWT later

            RateLimiter rateLimiter = getRateLimiterForUser(userId);

            if (rateLimiter.acquirePermission()) {
                return chain.filter(exchange);
            } else {
                logger.warn("Rate limit exceeded for user: {}", userId);
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return response.setComplete();
            }
        };
    }

    private RateLimiter getRateLimiterForUser(String userId) {
        return userRateLimiters.computeIfAbsent(userId != null ? userId : "anonymous",
                id -> rateLimiterRegistry.rateLimiter("userRateLimit"));
    }

    private String getUserIdFromRequest(ServerWebExchange exchange) {
        // TODO: Extract from JWT token in header (we'll do this in next step)
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Simple extraction for now - will improve with JWT filter
            return authHeader.substring(7).hashCode() + "";
        }
        return null;
    }

    public static class Config {
        // Empty for now
    }
}