package com.githubpilot.githubP.Service.Github;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.NameNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.githubpilot.githubP.DTO.IndexStatusReponse;
import com.githubpilot.githubP.DTO.RepoResDto;
import com.githubpilot.githubP.Entity.Repository;
import com.githubpilot.githubP.Entity.user;
import com.githubpilot.githubP.Repository.RepoRepository;
import com.githubpilot.githubP.Service.userService;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class RepoService {

    private final RepoRepository repoRepository;
    private final userService userService;
    private final githubApiClient githubApiClient;


    //get all the repos of user and saved into the db
    @Transactional
   public List<RepoResDto> syncAndListRepo(UUID userId) {

    user user = userService.requiredById(userId);

    String token = userService.decryptAccessToken(user);


    List<Map<String, Object>> remoteRepos =
            githubApiClient.listAllRepos(token);

    List<Repository> saved = new ArrayList<>();

    for (Map<String, Object> remote : remoteRepos) {

        Long githubRepoId = toLong(remote.get("id"));

        Repository repo = repoRepository
                .findByUserIdAndGithubRepoId(userId, githubRepoId)
                .orElseGet(Repository::new);

        String fullName = String.valueOf(remote.get("full_name"));

        String[] parts = fullName.split("/", 2);

        repo.setUserId(userId);

        repo.setGithubRepoId(githubRepoId);

        repo.setOwner(
                parts.length > 0
                        ? parts[0]
                        : String.valueOf(remote.get("owner"))
        );

        repo.setName(
                parts.length > 1
                        ? parts[1]
                        : String.valueOf(remote.get("name"))
        );

        repo.setFullName(fullName);

        repo.setPrivate(
                Boolean.TRUE.equals(remote.get("private"))
        );

        repo.setDefaultBranch(
                remote.get("default_branch") != null
                        ? String.valueOf(remote.get("default_branch"))
                        : "main"
        );

        repo.setLanguage(
                remote.get("language") != null
                        ? String.valueOf(remote.get("language"))
                        : null
        );

        repo.setHtmlUrl(
    remote.get("html_url") != null
        ? String.valueOf(remote.get("html_url"))
        : null
);

        repo.setDescription(
                remote.get("description") != null
                        ? String.valueOf(remote.get("description"))
                        : null
        );

        if (repo.getOwner() == null || repo.getOwner().isBlank()) {

            Object ownerObj = remote.get("owner");

            if (ownerObj instanceof Map<?, ?> ownerMap
                    && ownerMap.get("login") != null) {

                repo.setOwner(
                        String.valueOf(ownerMap.get("login"))
                );
            }
        }

        saved.add(repoRepository.save(repo));
    }



    return saved.stream().sorted((a,b)-> a.getFullName().compareToIgnoreCase(b.getFullName()))
    .map(this::toResponse)
    .toList();
}


public RepoResDto toResponse(Repository repository)
{
    return new RepoResDto(
        repository.getId(),
        repository.getGithubRepoId(),
        repository.getOwner(),
        repository.getName(),
        repository.getFullName(),
        repository.isPrivate(),
        repository.getDefaultBranch(),
        repository.getLanguage(),
        repository.getHtmlUrl(),
        repository.getDescription(),
        repository.getIndexStatus(),
        repository.getIndexAt(),
        repository.getChunkCount(),
        repository.getFilesTotal(),
        repository.getFilesProcessed(),
        repository.getErrorMessage()
    );
}

@Transactional
public List<RepoResDto> listSorted(UUID userId)
{
    return repoRepository.findByUserIdOrderByFullNameAsc(userId).stream()
    .map(this::toResponse)
    .toList();
}

@Transactional
public Repository requiredOwned(UUID repoId, UUID userId) throws Exception
{
   return repoRepository.findByIdAndUserId(repoId, userId).orElseThrow(()-> new NameNotFoundException("Repository not found"));
}


@Transactional
public IndexStatusReponse status(UUID repoId, UUID userId) throws Exception
{
   Repository repo= requiredOwned(repoId, userId);

   return new IndexStatusReponse(
    repo.getId(),
    repo.getIndexStatus(),
    repo.getFilesTotal(),
    repo.getFilesProcessed(),
    repo.getChunkCount(),
    repo.getIndexAt(),
    repo.getErrorMessage()
   );
   


}


    private static Long toLong(Object value)
    {
        if( value instanceof Number number){
            return number.longValue();
        }

        return Long.parseLong(String.valueOf(value));
    }


    
}
