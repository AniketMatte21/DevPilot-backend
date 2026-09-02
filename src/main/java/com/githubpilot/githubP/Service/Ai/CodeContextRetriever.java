package com.githubpilot.githubP.Service.Ai;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Service;

import com.githubpilot.githubP.Entity.Repository;
import com.githubpilot.githubP.Repository.RepoRepository;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CodeContextRetriever {

    private static final String NO_MATCHES = "(no matching code chunks found)";
private final RepoRepository repoRepository;
private final VectorStore vectorStore;
private final CitationMapper citationMapper;

/*
 * CodeContextRetriever:
 * Retrieves the most relevant code chunks from the vector database
 * based on the user's question and repository ID.
 *
 * Flow:
 * 1. Filters search results by repository ID.
 * 2. Performs similarity search using the user's question.
 * 3. Gets the top K relevant code chunks.
 * 4. Converts document metadata into CitationDto objects.
 * 5. Combines retrieved code chunks into a single contextText.
 * 6. Returns both the code context and citations as RetrievedContext.
 */


public RetrievedContext retrieve(UUID repositoryId, String question) {

    var filter = new FilterExpressionBuilder()
            .eq(RagSettings.METADATA_REPO_ID, repositoryId.toString())
            .build();

    var search = SearchRequest.builder()
            .query(question)
            .topK(RagSettings.TOP_K_CHUNKS)
            .filterExpression(filter)
            .build();

    var documents = vectorStore.similaritySearch(search);
    Repository repo = repoRepository.findById(repositoryId)
            .orElseThrow(() -> new RuntimeException("Repository not found"));

    var citations = documents.stream()
            .map(document -> citationMapper.fromDocument(document, repo))
            .distinct()
            .toList();


    var contextText = documents.stream()
            .map(Document::getText)
            .collect(Collectors.joining("\n\n---\n\n"));

    if (contextText.isBlank()) {
        contextText = NO_MATCHES;
    }

    return new RetrievedContext(citations, contextText);
}



    
}
