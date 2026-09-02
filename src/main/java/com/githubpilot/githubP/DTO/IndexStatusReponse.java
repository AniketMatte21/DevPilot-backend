package com.githubpilot.githubP.DTO;

import java.time.Instant;
import java.util.UUID;

import com.githubpilot.githubP.Entity.IndexStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class IndexStatusReponse {

    UUID repositoryId;
    IndexStatus indexStatus;
    int filesTotal;
    int filesProcessed;
    int chunkCount;
    Instant indexedAt;
    String errorMessage;
    
}
