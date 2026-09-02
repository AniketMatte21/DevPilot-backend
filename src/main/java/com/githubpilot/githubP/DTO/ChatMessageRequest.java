package com.githubpilot.githubP.DTO;



import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
         String content
) {
}
