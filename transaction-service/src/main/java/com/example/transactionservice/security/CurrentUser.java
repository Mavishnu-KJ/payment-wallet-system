package com.example.transactionservice.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public String getCurrentUsername(){

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            return null;
        }

        Object principal = authentication.getPrincipal();

        if(principal instanceof String p){
            return p;
        }

        return null;
    }
}
