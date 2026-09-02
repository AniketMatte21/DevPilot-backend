package com.githubpilot.githubP.Service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.githubpilot.githubP.DTO.ChatMessageResponse;
import com.githubpilot.githubP.DTO.ChatSessionResponse;
import com.githubpilot.githubP.DTO.CreateChatSessionRequest;
import com.githubpilot.githubP.Entity.ChatMessage;
import com.githubpilot.githubP.Entity.ChatSession;
import com.githubpilot.githubP.Entity.IndexStatus;
import com.githubpilot.githubP.Entity.MessageRole;
import com.githubpilot.githubP.Entity.Repository;
import com.githubpilot.githubP.Repository.ChatMessageRepository;
import com.githubpilot.githubP.Repository.ChatSessionRepository;
import com.githubpilot.githubP.Service.Ai.ChatPromptBuilder;
import com.githubpilot.githubP.Service.Ai.ChatStreamHandler;
import com.githubpilot.githubP.Service.Ai.CitationMapper;
import com.githubpilot.githubP.Service.Ai.CodeContextRetriever;
import com.githubpilot.githubP.Service.Github.RepoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final RepoService repoService;

    private final CodeContextRetriever codeContextRetriever;

    private final ChatPromptBuilder chatPromptBuilder;

    private final ChatStreamHandler chatStreamHandler;

    private final CitationMapper citationMapper;

    @Transactional
    public ChatSessionResponse createSession(
            UUID userId,
            CreateChatSessionRequest request
    ) throws Exception{

        Repository repo =
                repoService.requiredOwned(
                        request.repositoryId(),
                        userId
                );

        if (repo.getIndexStatus() != IndexStatus.READY) {

            throw new IllegalStateException(
                    "Repository must be indexed before chatting"
            );
        }

        String title =
                request.title() != null &&
                !request.title().isBlank()
                        ? request.title()
                        : "Chat with " + repo.getFullName();

        ChatSession session =
                ChatSession.builder()
                        .userId(userId)
                        .repositoryId(repo.getId())
                        .title(title)
                        .build();

        session = chatSessionRepository.save(session);

        return toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> listSessions(
            UUID userId,
            UUID repositoryId
    ) throws Exception {

        repoService.requiredOwned(
                repositoryId,
                userId
        );

        return chatSessionRepository
                .findByUserIdAndRepositoryIdOrderByCreatedAtDesc(
                        userId,
                        repositoryId
                )
                .stream()
                .map(this::toSessionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(
            UUID userId,
            UUID sessionId
    ) throws Exception{

        ChatSession session =
                requireSession(
                        userId,
                        sessionId
                );

        return chatMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(
                        session.getId()
                )
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatSession requireSession(
            UUID userId,
            UUID sessionId
    ) throws Exception{

        return chatSessionRepository
                .findByIdAndUserId(
                        sessionId,
                        userId
                )
                .orElseThrow(
                        () -> new Exception(
                                "Chat session not found"
                        )
                );
    }

    public SseEmitter streamReply(
            UUID userId,
            UUID sessionId,
            String userContent
    ) throws Exception {

        // 1. Ensure session exists and repo is indexed

        ChatSession session =
                requireSession(
                        userId,
                        sessionId
                );

        Repository repo =
                repoService.requiredOwned(
                        session.getRepositoryId(),
                        userId
                );

        if (repo.getIndexStatus() != IndexStatus.READY) {

            throw new Exception(
                    "Repository is not ready for chat"
            );
        }

        // 2. Persist the user's message

        ChatMessage userMessage =
                chatMessageRepository.save(
                        ChatMessage.builder()
                                .sessionId(session.getId())
                                .role(MessageRole.USER)
                                .content(userContent)
                                .build()
                );

        // 3. RAG retrieval - find code chunks
        //    similar to the question

        var retrievedContext =
                codeContextRetriever.retrieve(
                        repo.getId(),
                        userContent
                );

        // 4. Build LLM prompts from retrieved context
        //    + question

        String systemPrompt =
                chatPromptBuilder.systemPrompt(
                        repo.getFullName()
                );

        String userPrompt =
                chatPromptBuilder.userPrompt(
                        retrievedContext.contextText(),
                        userContent
                );

        // 5. Stream OpenAI/LLM response
        //    to the client using SSE

        return chatStreamHandler.stream(
                session.getId(),
                toMessageResponse(userMessage),
                retrievedContext.citations(),
                systemPrompt,
                userPrompt
        );
    }

    private ChatSessionResponse toSessionResponse(
            ChatSession session
    ) {

        return new ChatSessionResponse(
                session.getId(),
                session.getRepositoryId(),
                session.getTitle(),
                session.getCreatedAt()
        );
    }

    private ChatMessageResponse toMessageResponse(
            ChatMessage message
    ) {

        return new ChatMessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                citationMapper.fromJson(
                        message.getCitations()
                ),
                message.getCreatedAt()
        );
    }
}