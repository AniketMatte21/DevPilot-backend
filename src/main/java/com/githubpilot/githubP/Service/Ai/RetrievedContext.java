package com.githubpilot.githubP.Service.Ai;

import java.util.List;

import com.githubpilot.githubP.DTO.CitationDto;

public record RetrievedContext(
    List<CitationDto> citations,
    String contextText
) {
    
}
