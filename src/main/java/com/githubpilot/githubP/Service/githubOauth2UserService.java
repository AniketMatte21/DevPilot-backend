package com.githubpilot.githubP.Service;

import java.util.Set;


import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.githubpilot.githubP.Config.AppUserPrincipal;
import com.githubpilot.githubP.Entity.user;

@Service
public class githubOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
            new DefaultOAuth2UserService();

    
    @Autowired
    private userService userService;

   // Line 21: This function is called when user authorizes your app
public OAuth2User loadUser(OAuth2UserRequest userRequest) 
    throws OAuth2AuthenticationException {
    
    // Line 22: Get the raw OAuth2User from GitHub
    OAuth2User githubUser = delegate.loadUser(userRequest);
    
    // Line 24: Extract access token
    String accessToken = userRequest
        .getAccessToken()
        .getTokenValue();
    
    // Line 25-26: Get scopes (permissions) user granted
    Set<String> scopes = userRequest
        .getAccessToken()
        .getScopes() ;// e.g., ["read:user", "repo"]

        
// System.out.println("🔥 GitHub scopes = " + scopes);
        
    // Line 29: Call UserService to create/update user in YOUR database
    user user = userService.upsertFromGitHub(
        githubUser.getAttributes(),  // GitHub data: {login, avatar_url, ...}
        accessToken,                  // Token from GitHub
        scopes                         // What permissions user gave
    );
    
    // Line 30: Return AppUserPrincipal (Spring's format)
    return new AppUserPrincipal(user, githubUser.getAttributes());
}
}
