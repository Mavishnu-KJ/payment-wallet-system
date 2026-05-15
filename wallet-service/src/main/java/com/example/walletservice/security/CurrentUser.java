package com.example.walletservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CurrentUser {

    private static final Logger logger = LoggerFactory.getLogger(CurrentUser.class);

    /**
     * Priority 1: X-User-Id from API Gateway (Recommended)
     * Priority 2: Fallback to Spring Security Context (for direct calls on port 8082)
     */
    public Long getCurrentUserId() {
        logger.info("getCurrentUserId, entered");

        String userIdFromHeader = getHeader("X-User-Id");
        logger.info("getCurrentUserId, userIdFromHeader is {}", userIdFromHeader);

        if (userIdFromHeader != null && !userIdFromHeader.isBlank()) {
            return Long.valueOf(userIdFromHeader);
        }

        // Fallback for direct calls
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("getCurrentUserId, authentication is {}", authentication);

        if (authentication != null && authentication.isAuthenticated()) {
            // You can return userId if you store it in authentication, else return null
            return null;
        }
        return null;
    }

    public String getCurrentUsername() {
        logger.info("getCurrentUsername, entered");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("getCurrentUsername, authentication is {}", authentication);

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            logger.info("getCurrentUsername, principal is {}", principal);

            if (principal instanceof String p) {
                logger.info("getCurrentUsername, principal p is {}", p);
                return p;
            }
        }
        return null;
    }

    private String getHeader(String headerName) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            return request.getHeader(headerName);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
}