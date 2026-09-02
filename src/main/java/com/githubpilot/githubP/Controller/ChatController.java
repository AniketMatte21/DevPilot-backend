package com.githubpilot.githubP.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.githubpilot.githubP.Config.CurrentUser;
import com.githubpilot.githubP.DTO.ChatMessageRequest;
import com.githubpilot.githubP.DTO.ChatMessageResponse;
import com.githubpilot.githubP.DTO.ChatSessionResponse;
import com.githubpilot.githubP.DTO.CreateChatSessionRequest;
import com.githubpilot.githubP.Entity.ChatSession;
import com.githubpilot.githubP.Service.ChatService;


import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final CurrentUser currentUser;
    private final ChatService chatService;


    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponse> createSessioEntity(@RequestBody CreateChatSessionRequest request) throws Exception
    {
        System.out.println("repo id "+ request.repositoryId());
        UUID userId= currentUser.getId();
        return ResponseEntity.ok(chatService.createSession(userId,request));
    }

    @GetMapping("/sessions")
    public List<ChatSessionResponse> listSessions(@RequestParam UUID repositoryId ) throws Exception
    {
        UUID userId=currentUser.getId();
        return chatService.listSessions(userId, repositoryId);
    }

    @GetMapping("/sessions/{id}")
    public List<ChatMessageResponse> getMessage(@PathVariable UUID id) throws Exception
    {
        UUID userId= currentUser.getId();
        return chatService.getMessages(userId, id);
    }

    @PostMapping(value = "/sessions/{id}/messages", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter sendMessage(
        @PathVariable UUID id,
        @RequestBody ChatMessageRequest request
    ) throws Exception
    {
        UUID userId= currentUser.getId();
        return chatService.streamReply(userId, id, request.content());
    }
    
}
