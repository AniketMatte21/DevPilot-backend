package com.githubpilot.githubP.Service.Ai;

public class RagSettings {

    // How many code chunks to fetch from vector db
    public static final int TOP_K_CHUNKS=8;

    //Maximum time to keep an SSE stream open while model is responsing
    public static final long STREAM_TIMEOUT_MS=180_000L;

    public static final String METADATA_REPO_ID="repoId";

    private RagSettings(){}




    
}
