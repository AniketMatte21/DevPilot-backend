package com.githubpilot.githubP.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.githubpilot.githubP.Config.CurrentUser;
import com.githubpilot.githubP.DTO.IndexStatusReponse;
import com.githubpilot.githubP.DTO.RepoResDto;
import com.githubpilot.githubP.Entity.Repository;
import com.githubpilot.githubP.Service.Github.RepoService;
import com.githubpilot.githubP.Service.Indexing.IndexingService;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoController {

    private final CurrentUser currentUser;
    private final RepoService repoService;
    private final IndexingService indexingService;
    

 @GetMapping
public List<RepoResDto> list(
        @RequestParam(defaultValue = "false") boolean refresh
) {
    UUID userId = currentUser.getId();

    if (refresh) {
        return repoService.syncAndListRepo(userId);
    }

    return repoService.listSorted(userId);
}
    @GetMapping("/{id}")
    public RepoResDto getUniqueRepo(@PathVariable UUID id) throws Exception
    {
        UUID userId= currentUser.getId();
        return repoService.toResponse(repoService.requiredOwned(id, userId));
    }
    

    @GetMapping("/{id}/status")
    public IndexStatusReponse stats(@PathVariable UUID id) throws Exception
    {
        UUID userId= currentUser.getId();
        return repoService.status(id, userId);
    }

    @PostMapping("/{id}/index")
    public ResponseEntity<RepoResDto> index(@PathVariable UUID id)
    {
        UUID userId= currentUser.getId();
        Repository repo= indexingService.startIndexing(id,userId);
        indexingService.indexAsync(id, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(repoService.toResponse(repo));


    }
}
