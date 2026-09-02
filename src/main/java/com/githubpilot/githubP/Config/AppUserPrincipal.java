package com.githubpilot.githubP.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.githubpilot.githubP.Entity.user;

public class AppUserPrincipal implements OAuth2User {

    private user user;                          // From DATABASE
    private Map<String, Object> attributes;     // From GITHUB

    public AppUserPrincipal(user user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    public user getUser() {
        return this.user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return String.valueOf(user.getGithubId());
    }
}