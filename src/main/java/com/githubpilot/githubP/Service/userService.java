package com.githubpilot.githubP.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import com.githubpilot.githubP.Entity.user;
import com.githubpilot.githubP.Repository.userRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class userService {

    @Autowired
    private userRepository userRepository;
    
    @Autowired
    public final TextEncryptor textEncryptor;

    //find user 
    @Transactional
    public user requiredById(UUID id)
    {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(" user not found "));
    }
    
    @Transactional
    public String decryptAccessToken(user user)
    {
        System.out.println("access  token"+ textEncryptor.decrypt(user.getAccessToken()));
        return textEncryptor.decrypt(user.getAccessToken());
    }

    private static Long toLong(Object value)
    {
        if( value instanceof Number number) return number.longValue();

        return Long.parseLong(String.valueOf(value));
    }

    // Line 22: Main function - Create or Update user
public user upsertFromGitHub(
    Map<String, Object> attributes,  // GitHub data
    String accessToken,               // Token from GitHub
    Set<String> scopes
) {
    
    // Line 24: Get GitHub user ID
    Long githubId = toLong(attributes.get("id"));
    
    // Line 25-26: Get username and display name
    String login = String.valueOf(attributes.get("login"));
    String name = attributes.get("name") != null 
        ? String.valueOf(attributes.get("name")) 
        : login;
    
    // Line 28-29: Get avatar URL
    String avatarUrl = attributes.get("avatar_url") != null 
        ? String.valueOf(attributes.get("avatar_url")) 
        : null;
    
    // Line 32: ENCRYPT token before storing in database
    String encryptedToken = textEncryptor.encrypt(accessToken);
    System.out.println("encrypted token hai "+encryptedToken);
    
    // Line 34-35: Check if user exists
    user user = userRepository
        .findByGithubId(githubId)     // Already exists?
        .orElseGet(() -> new user());  // Create new if not
    
    // Line 36-40: Set user properties
    user.setGithubId(githubId);
    user.setGithubUsername(login);
    user.setDisplayName(name);
    user.setAvatarUrl(avatarUrl);
    user.setAccessToken(encryptedToken);
    user.setTokenScope(String.join(",", scopes));
    
    // Line 41: Save to database
    return userRepository.save(user);
}

}
