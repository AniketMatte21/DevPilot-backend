package com.githubpilot.githubP.Service.Indexing;

import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class codeFileFilter {

    private static final Set<String> SKIP_DIR_PARTS = Set.of(
    ".git",
    ".svn",
    ".hg",

    "node_modules",
    "bower_components",

    "target",
    ".gradle",

    "__pycache__",
    ".pytest_cache",
    ".mypy_cache",
    ".ruff_cache",
    ".venv",
    "venv",

    ".idea",
    ".vscode",
    ".settings",

    "coverage",
    ".nyc_output",

    "vendor",

    "bin",
    "obj",

    ".terraform",

    "Pods",
    "DerivedData",

    ".dart_tool",

    ".DS_Store"
);

private static final Set<String> ALLOWED_EXTENSIONS = Set.of(

    // Java / JVM
    ".java",
    ".kt",
    ".kts",
    ".scala",
    ".groovy",

    // JavaScript / TypeScript
    ".js",
    ".jsx",
    ".ts",
    ".tsx",
    ".mjs",
    ".cjs",

    // Web
    ".html",
    ".htm",
    ".css",
    ".scss",
    ".sass",
    ".less",
    ".vue",
    ".svelte",

    // Python
    ".py",
    ".pyw",
    ".pyi",

    // C / C++
    ".c",
    ".h",
    ".cc",
    ".cpp",
    ".cxx",
    ".hpp",
    ".hh",
    ".hxx",

    // C#
    ".cs",
    ".csx",

    // Go
    ".go",

    // Rust
    ".rs",

    // PHP
    ".php",
    ".phtml",

    // Ruby
    ".rb",
    ".rake",

    // Swift / Objective-C
    ".swift",
    ".m",
    ".mm",

    // Kotlin / Android
    ".aidl",

    // Dart / Flutter
    ".dart",

    // Shell / Scripts
    ".sh",
    ".bash",
    ".zsh",
    ".fish",
    ".ps1",
    ".bat",
    ".cmd",

    // SQL
    ".sql",

    // R
    ".r",
    ".R",

    // Lua
    ".lua",

    // Perl
    ".pl",
    ".pm",

    // Elixir / Erlang
    ".ex",
    ".exs",
    ".erl",
    ".hrl",

    // Haskell
    ".hs",
    ".lhs",

    // Scala
    ".sc",

    // Functional languages
    ".clj",
    ".cljs",
    ".fs",
    ".fsx",
    ".fsi",
    ".ml",
    ".mli",

    // Configuration
    ".xml",
    ".json",
    ".yaml",
    ".yml",
    ".toml",
    ".ini",
    ".properties",
    ".conf",
    ".config",

    // Build / Dependency files
    ".gradle",
    ".pom",

    // Docker
    ".dockerfile",

    // GraphQL
    ".graphql",
    ".gql",

    // Protocol / API definitions
    ".proto",

    // Documentation
    ".md",
    ".mdx",
    ".rst",
    ".txt",

    // Templates
    ".ftl",
    ".mustache",
    ".hbs",
    ".handlebars",
    ".ejs",

    // Other common source files
    ".asm",
    ".s",
    ".sol",
    ".vim"
);

private static final Set<String> SKIP_FILE_NAMES = Set.of(

    // Git
    ".gitignore",
    ".gitattributes",
    ".gitmodules",

    // Node / JavaScript
    "package-lock.json",
    "yarn.lock",
    "pnpm-lock.yaml",
    "npm-shrinkwrap.json",

    // Java / Maven / Gradle
    "maven-wrapper.jar",
    "gradlew",
    "gradlew.bat",

    // Python
    "Pipfile.lock",
    "poetry.lock",
    "pdm.lock",

    // Rust
    "Cargo.lock",

    // Go
    "go.sum",

    // PHP
    "composer.lock",

    // Ruby
    "Gemfile.lock",

    // .NET
    "packages.lock.json",

    // IDE / Editor
    ".classpath",
    ".project",
    ".factorypath",

    // OS generated
    ".DS_Store",
    "Thumbs.db",
    "Desktop.ini",

    // Environment / secrets
    ".env",
    ".env.local",
    ".env.development",
    ".env.production",
    ".env.test",

    // Logs
    "npm-debug.log",
    "yarn-debug.log",
    "yarn-error.log",

    // Generated metadata
    "tsconfig.tsbuildinfo",

    // Coverage
    ".coverage",

    // Minified / generated common files
    "robots.txt",
    "sitemap.xml"
);

    public boolean isEligible(
            String path,
            long sizeBytes,
            long maxFileBytes
    ) {

        // 1. Invalid path
        if (path == null || path.isBlank()) {
            return false;
        }

        // 2. Normalize path
        String normalized = path.replace('\\', '/');
        String lower = normalized.toLowerCase(Locale.ROOT);

        // 3. Check skipped directories
        for (String part : lower.split("/")) {

            if (SKIP_DIR_PARTS.contains(part)) {
                return false;
            }
        }

        // 4. Get file name
        String fileName =
                lower.substring(lower.lastIndexOf('/') + 1);

        // 5. Check skipped file names
        if (SKIP_FILE_NAMES.contains(fileName)) {
            return false;
        }

        // 6. Skip hidden files
        if (fileName.startsWith(".")) {
            return false;
        }

        // 7. Check file size
        if (sizeBytes < 0 || sizeBytes > maxFileBytes) {
            return false;
        }

        // 8. Files without extensions but useful for indexing
        if ("dockerfile".equals(fileName)
                || "makefile".equals(fileName)
                || "jenkinsfile".equals(fileName)
                || "procfile".equals(fileName)) {

            return true;
        }

        // 9. Find extension
        int dot = fileName.lastIndexOf('.');

        // No extension
        if (dot <= 0 || dot == fileName.length() - 1) {
            return false;
        }

        String extension = fileName.substring(dot);

        // 10. Check allowed extension
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    public String detectLanguage(String path) {

    String lower = path.toLowerCase(Locale.ROOT);

    String fileName =
            lower.substring(lower.lastIndexOf('/') + 1);

    // Files without extensions
    if ("dockerfile".equals(fileName)) {
        return "dockerfile";
    }

    if ("makefile".equals(fileName)) {
        return "makefile";
    }

    // Find extension
    int dot = fileName.lastIndexOf('.');

    // No extension
    if (dot < 0) {
        return "text";
    }

    // Return extension as language
    return fileName.substring(dot + 1);
}
    
}
