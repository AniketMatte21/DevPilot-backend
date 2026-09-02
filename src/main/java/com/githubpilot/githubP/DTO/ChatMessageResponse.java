package com.githubpilot.githubP.DTO;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.githubpilot.githubP.Entity.MessageRole;


public record ChatMessageResponse(
        UUID id,
        MessageRole role,
        String content,
        List<CitationDto> citations,
        Instant createdAt
) {
}
