package com.githubpilot.githubP.Repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import com.githubpilot.githubP.Entity.Repository;



public interface RepoRepository extends JpaRepository<Repository,UUID>{

    List<Repository> findByUserIdOrderByFullNameAsc(UUID userId);
    Optional<Repository> findByIdAndUserId(UUID id, UUID userId);
    Optional<Repository> findByUserIdAndGithubRepoId(UUID userId, Long githubRepoId);
    
    
}
