package com.githubpilot.githubP.Service.Indexing;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.githubpilot.githubP.Config.rateLimiter;
import com.githubpilot.githubP.Entity.IndexStatus;
import com.githubpilot.githubP.Entity.Repository;
import com.githubpilot.githubP.Repository.RepoRepository;
import com.githubpilot.githubP.Service.userService;
import com.githubpilot.githubP.Service.Ai.RagSettings;
import com.githubpilot.githubP.Service.Github.githubApiClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingService {

    private static final int VECTOR_BATCH_SIZE=32;
    private static final int PROGRESS_EVERY_N_FILES=5;

    private final RepoRepository repoRepository;
    private final userService userService;
    private final githubApiClient githubApiClient;
    private final codeFileFilter fileFilter;
    private final codeChunker codeChunker;
    private final VectorStore vectorStore;
    private final rateLimiter rateLimiter;

    @Value("${app.indexing.max-file-bytes:102400}")
    private  long maxFileBytes;

    
    public Repository startIndexing(UUID repoId, UUID userId) {

    Repository repo = repoRepository.findById(repoId)
            .orElseThrow(() -> new RuntimeException("Repository not found"));

    if (!repo.getUserId().equals(userId)) {
        throw new RuntimeException("Unauthorized");
    }

    if (repo.getIndexStatus() == IndexStatus.INDEXING) {
        throw new RuntimeException("Indexing already in progress");
    }

    repo.setIndexStatus(IndexStatus.INDEXING);
    repo.setFilesTotal(0);
    repo.setFilesProcessed(0);
    repo.setChunkCount(0);
    repo.setIndexAt(null);
    repo.setErrorMessage(null);

    return repoRepository.save(repo);
}

@Async("indexingExecuter")
public void indexAsync(UUID repoId, UUID userId) {

    try {
        doIndex(repoId, userId);
    } catch (Exception e) {
        markFailed(repoId, e.getMessage());
    }
}

private void doIndex(UUID repoId, UUID userId) {

    Repository repo = repoRepository.findById(repoId)
            .orElseThrow(() -> new RuntimeException("Repository not found"));

    String token = userService.decryptAccessToken(
            userService.requiredById(userId)
    );

    deleteExistingVectors(repoId.toString());

    Map<String, Object> tree =
            githubApiClient.getRepoTree(
                    token,
                    repo.getOwner(),
                    repo.getName(),
                    repo.getDefaultBranch()
            );

    List<String> filePaths = listIndexableFiles(tree);

    updateProgress(
            repoId,
            filePaths.size(),
            0,
            0,
            IndexStatus.INDEXING,
            null
    );

    List<Document> batch = new ArrayList<>();

    int processed = 0;
    int totalChunks = 0;

    for (String path : filePaths) {

        try {

            String content =
                    githubApiClient.getFileContent(
                            token,
                            repo.getOwner(),
                            repo.getName(),
                            path
                    );

            List<Document> chunks =
                    codeChunker.chunkFile(
                            repoId.toString(),
                            path,
                            content
                    );

            batch.addAll(chunks);
            totalChunks += chunks.size();

            if (batch.size() >= VECTOR_BATCH_SIZE) {
                vectorStore.add(batch);
                batch.clear();
            }

            processed++;

            if (processed % PROGRESS_EVERY_N_FILES == 0
                    || processed == filePaths.size()) {

                updateProgress(
                        repoId,
                        filePaths.size(),
                        processed,
                        totalChunks,
                        IndexStatus.INDEXING,
                        null
                );
            }

            rateLimiter.pause();

        } catch (Exception e) {

            // skip file and continue
            log.warn(
                    "Failed to index file {}: {}",
                    path,
                    e.getMessage()
            );
        }
    }

    if (!batch.isEmpty()) {
        vectorStore.add(batch);
    }

    markReady(
            repoId,
            filePaths.size(),
            processed,
            totalChunks,
            repo.getFullName()
    );
}

@SuppressWarnings("unchecked")
private List<String> listIndexableFiles(Map<String, Object> tree) {

    if (tree == null || tree.get("tree") == null) {
        return List.of();
    }

    List<Map<String, Object>> entries =
            (List<Map<String, Object>>) tree.get("tree");

    return entries.stream()

            .filter(entry ->
                    "blob".equals(
                            String.valueOf(entry.get("type"))
                    )
            )

            .filter(entry -> {

                String path =
                        String.valueOf(entry.get("path"));

                long size =
                        entry.get("size") instanceof Number n
                                ? n.longValue()
                                : 0L;

        

                return fileFilter.isEligible(
                        path,
                        size,
                        maxFileBytes
                );
            })

            .map(entry ->
                    String.valueOf(entry.get("path"))
            )

            .toList();
}

public void deleteExistingVectors(String repoId)
{
    try{
        var filter=new FilterExpressionBuilder().eq(RagSettings.METADATA_REPO_ID, repoId).build();
        vectorStore.delete(filter);
    }catch(Exception ex)
    {
        log.warn("Could not delete existing vectors from repo {}: {}", repoId, ex.getMessage());
    }
}


@Transactional
protected void updateProgress(
        UUID repoId,
        int totalFiles,
        int processedFiles,
        int totalChunks,
        IndexStatus status,
        String errorMessage) {

    repoRepository.findById(repoId).ifPresent(repo -> {

        repo.setIndexStatus(status);
        repo.setFilesTotal(totalFiles);
        repo.setFilesProcessed(processedFiles);
        repo.setChunkCount(totalChunks);
        repo.setErrorMessage(errorMessage);
        repo.setUpdatedAt(Instant.now());

        repoRepository.save(repo);
    });
}

@Transactional
protected void markReady(
        UUID repoId,
        int totalFiles,
        int processedFiles,
        int totalChunks,
        String fullName) {

    repoRepository.findById(repoId).ifPresent(repo -> {

        repo.setIndexStatus(IndexStatus.READY);
        repo.setFilesTotal(totalFiles);
        repo.setFilesProcessed(processedFiles);
        repo.setChunkCount(totalChunks);
        repo.setIndexAt(Instant.now());
        repo.setErrorMessage(null);
        repo.setUpdatedAt(Instant.now());

        repoRepository.save(repo);
    });

    log.info(
            "Indexed {} files ({} chunks) for {}",
            processedFiles,
            totalChunks,
            fullName
    );
}

@Transactional
protected void markFailed(UUID repoId, String message) {

    repoRepository.findById(repoId).ifPresent(repo -> {

        repo.setIndexStatus(IndexStatus.FAILED);

        repo.setErrorMessage(
                message != null && message.length() > 2000
                        ? message.substring(0, 2000)
                        : message
        );

        repo.setUpdatedAt(Instant.now());

        repoRepository.save(repo);
    });
}
    
}

