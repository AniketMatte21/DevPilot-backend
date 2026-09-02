package com.githubpilot.githubP.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.githubpilot.githubP.Config.AppUserPrincipal;
import com.githubpilot.githubP.Config.CurrentUser;
import com.githubpilot.githubP.Entity.user;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthController {

    private final CurrentUser currentUser;

    @GetMapping("/login")
    public Map<String,String> loginUrl(@Value("${app.backend-url}")String backendUrl)
    {
        return Map.of("url", backendUrl+"/oauth2/authorization/github");
    }

    @GetMapping("/me")
    public ResponseEntity<user> getUserInfo()
    {
        AppUserPrincipal principal=currentUser.require();
        user user=principal.getUser();
        return ResponseEntity.ok(user);
    }

    
    
}
