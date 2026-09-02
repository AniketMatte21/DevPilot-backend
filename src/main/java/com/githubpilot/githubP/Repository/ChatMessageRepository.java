package com.githubpilot.githubP.Repository;



import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.githubpilot.githubP.Entity.ChatMessage;


public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(
            UUID sessionId
    );
}
