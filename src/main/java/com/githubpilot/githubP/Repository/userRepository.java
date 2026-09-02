package com.githubpilot.githubP.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.githubpilot.githubP.Entity.user;

public interface userRepository extends JpaRepository<user,UUID>{

    Optional<user> findByGithubId(long githubId);
    
}
