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
import reactor.core.publisher.Mono;

@Component
public class RateLimitingFilter extends AbstractGatewayFilterFactory<RateLimitingFilter.Config> {

    private final static Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final RateLimiter rateLimiter;

    public RateLimitingFilter(RateLimiterRegistry rateLimiterRegistry) {
        super(Config.class);
        this.rateLimiter = rateLimiterRegistry.rateLimiter("defaultRateLimit");   // Global rate limiter
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (rateLimiter.acquirePermission()) {
                return chain.filter(exchange);
            } else {
                logger.warn("Rate limit exceeded for request: {}", exchange.getRequest().getURI());
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                return response.setComplete();
            }
        };
    }

    public static class Config {
        //private int limitForPeriod = 20;
        //private int limitRefreshPeriod = 1;   // in seconds
        // Empty for now - can be extended later
    }
}