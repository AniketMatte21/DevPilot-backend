package com.githubpilot.githubP.Config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

// Line 8: This is a Spring Component (injectable)
@Component
public class CurrentUser {


    
    // Line 8: Check who is authenticated
    public AppUserPrincipal require() {
        // Line 9: Get security context (Spring's storage of auth info)
        Authentication auth = SecurityContextHolder
            .getContext()
            .getAuthentication();
        
        // Line 10: Is user NOT authenticated?
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal)) {
            // Line 11: Throw error
            throw new AuthenticationCredentialsNotFoundException("Not authenticated");


        }


        
        // Line 13: Return current user
        return (AppUserPrincipal) auth.getPrincipal();
    }

    public UUID getId() {
        return require().getUser().getId();
    }
}