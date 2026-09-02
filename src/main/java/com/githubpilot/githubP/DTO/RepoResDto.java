package com.githubpilot.githubP.DTO;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.githubpilot.githubP.Entity.IndexStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class RepoResDto {

    UUID id;
    Long githubRepoId;
    String name;
    String owner;
    String fullName;
    @JsonProperty("isPrivate")
    boolean isPrivate;
    String defaultBranch;
    String language;
    String htmlUrl;
    String description;
    IndexStatus indexStatus;
    Instant indexedAt;
    int chunkCount;
    int filesTotal;
    int filesProcessed;
    String errorMessage;

    
}
