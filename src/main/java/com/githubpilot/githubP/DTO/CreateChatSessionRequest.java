package com.githubpilot.githubP.DTO;



import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateChatSessionRequest(
       UUID repositoryId,
        String title
) {
}