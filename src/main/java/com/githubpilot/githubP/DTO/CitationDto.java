package com.githubpilot.githubP.DTO;

public record CitationDto(
    String filePath,
    Integer startLine,
    Integer endLine,
    String language,
    String sourceUrl
) {

}