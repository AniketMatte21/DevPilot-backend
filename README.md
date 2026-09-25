## DevPilot — AI-Powered GitHub Repository Assistant

Devpilot is a Spring Boot–based AI developer assistant that integrates with GitHub to analyze repositories, index source code using Retrieval-Augmented Generation (RAG), and provide context-aware AI conversations about codebases.

The application retrieves repository metadata and source files through the GitHub API, processes and chunks code into searchable documents, and enriches each chunk with metadata such as repository ID, file path, programming language, and chunk index. This indexed context is then used to generate accurate, codebase-specific responses through an AI chat interface.

### Key Capabilities

GitHub Integration— Retrieves user repositories, repository trees, branches, and source files using the GitHub API.

Codebase Indexing — Processes repository files and converts source code into RAG-ready document chunks.

RAG Pipeline — Maintains metadata for repository, file path, programming language, and chunk position to enable contextual retrieval.

AI Codebase Chat — Generates context-aware responses based strictly on the indexed repository code.

Indexing Tracking — Tracks indexing status, total files, processed files, and generated chunk counts.

Chat Session Management — Supports creation and management of AI chat sessions for repository-specific conversations.

### Technology

Java • Spring Boot • GitHub API • REST APIs • RAG • AI/LLM Integration**

### Architecture Overview

```text
GitHub Repository
       ↓
GitHub API Integration
       ↓
Repository Tree & File Retrieval
       ↓
Code Processing & Chunking
       ↓
RAG Document Indexing
       ↓
Context Retrieval
       ↓
AI Prompt Generation
       ↓
AI Codebase Chat
```

DevPilot is designed to help developers understand, explore, and interact with large codebases using AI while keeping responses grounded in the actual repository context.**

### Indexing Workflow

<img width="1870" height="841" alt="devpilot-indexing workflow" src="https://github.com/user-attachments/assets/861eb9e7-3a49-4139-b6e8-6a6ddedbcab4" />





