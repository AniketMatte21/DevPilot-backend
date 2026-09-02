package com.githubpilot.githubP.Entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="Repositories", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","github_repo_id"})
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Repository {

   @Id
@GeneratedValue(strategy = GenerationType.UUID)
@Column(name = "id", nullable = false)
private UUID id;

@Column(name = "user_id", nullable = false)
private UUID userId;

@Column(name = "github_repo_id", nullable = false)
private long githubRepoId;

@Column(name = "owner", nullable = false, length = 100)
private String owner;

@Column(name = "name", nullable = false, length = 150)
private String name;

@Column(name = "full_name", nullable = false, length = 255)
private String fullName;

@Column(name = "is_private", nullable = false)
private boolean isPrivate;

@Column(name = "default_branch", nullable = false, length = 100)
private String defaultBranch;

@Column(name = "language", length = 100)
private String language;

@Column(name = "html_url", nullable = false, length = 500)
private String htmlUrl;

@Column(name = "description", length = 1000)
private String description;

@Enumerated(EnumType.STRING)
@Column(name = "index_status", nullable = false, length = 30)
private IndexStatus indexStatus = IndexStatus.PENDING;

@Column(name = "index_at")
private Instant indexAt;

@Column(name = "chunk_count", nullable = false)
private int chunkCount = 0;

@Column(name = "files_total", nullable = false)
private int filesTotal = 0;

@Column(name = "files_processed", nullable = false)
private int filesProcessed = 0;

@Column(name = "error_message", length = 2000)
private String errorMessage;

@Column(name = "created_at", nullable = false)
private Instant createdAt;

@Column(name = "updated_at", nullable = false)
private Instant updatedAt;


@PrePersist
void onCreate() {
    Instant now = Instant.now();

    if (createdAt == null)
        createdAt = now;

    updatedAt = now;

    if (indexStatus == null)
        indexStatus = IndexStatus.PENDING;
}

@PreUpdate
void onUpdate(){
    Instant now= Instant.now();

    updatedAt=now;
}




    
    
}
