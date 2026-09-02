package com.githubpilot.githubP.Entity;
import java.time.Instant;
import java.util.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="user_table")
@Builder
public class user {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="github_id", unique = true, nullable = false)
    private long githubId;

    @Column(name="github_username", nullable = false, length = 100)
    private String githubUsername;

    @Column(name="display_name", length = 100)
    private String displayName;

    @Column(name="avatar_url")
    private String avatarUrl;

    @Column(name="access_token",columnDefinition = "TEXT")
    private String accessToken;

    @Column(name="token_scope")
    private String tokenScope;

    @Column(name="created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate()
    {
        if( createdAt==null) createdAt=Instant.now();
    }

    
    
    
}
