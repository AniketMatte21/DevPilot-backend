package com.githubpilot.githubP.Service.Ai;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import com.githubpilot.githubP.DTO.CitationDto;
import com.githubpilot.githubP.Entity.Repository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class CitationMapper {

    private final JsonMapper jsonMapper;

    /*
     * CitationMapper:
     *
     * 1. Converts Document metadata into CitationDto.
     * 2. Builds a GitHub source URL for the cited file.
     * 3. Supports converting citations to JSON.
     * 4. Supports converting JSON back to citations.
     */

    // =====================================================
    // DOCUMENT -> CITATION DTO
    // =====================================================

    public CitationDto fromDocument(
            Document document,
            Repository repository
    ) {

        var meta = document.getMetadata();
            System.out.println("========== DOCUMENT METADATA ==========");
    System.out.println(meta);
    System.out.println("=======================================");

        String filePath =
                stringVal(meta.get("filePath"));

        Integer startLine =
                intVal(meta.get("startLine"));

        Integer endLine =
                intVal(meta.get("endLine"));

        String language =
                stringVal(meta.get("language"));

        String sourceUrl =
                buildSourceUrl(
                        repository,
                        filePath,
                        startLine,
                        endLine
                );

        return new CitationDto(
                filePath,
                startLine,
                endLine,
                language,
                sourceUrl
        );
    }


    // =====================================================
    // BUILD GITHUB SOURCE URL
    // =====================================================

    private String buildSourceUrl(
            Repository repository,
            String filePath,
            Integer startLine,
            Integer endLine
    ) {

        if (
                repository == null ||
                filePath == null ||
                filePath.isBlank()
        ) {
            return null;
        }

        String owner =
                repository.getOwner();

        String repoName =
                repository.getName();

        String branch =
                repository.getDefaultBranch();

        if (
                owner == null ||
                owner.isBlank() ||
                repoName == null ||
                repoName.isBlank()
        ) {
            return null;
        }

        if (
                branch == null ||
                branch.isBlank()
        ) {
            branch = "main";
        }

        /*
         * Encode only characters that can break
         * the GitHub URL.
         */
        String encodedPath =
                filePath
                        .replace(" ", "%20")
                        .replace("#", "%23");

        StringBuilder url =
                new StringBuilder();

        url.append("https://github.com/")
           .append(owner)
           .append("/")
           .append(repoName)
           .append("/blob/")
           .append(branch)
           .append("/")
           .append(encodedPath);


        // =================================================
        // LINE NUMBER
        // =================================================

        if (
                startLine != null &&
                startLine > 0
        ) {

            url.append("#L")
               .append(startLine);

            if (
                    endLine != null &&
                    endLine > 0 &&
                    !endLine.equals(startLine)
            ) {

                url.append("-L")
                   .append(endLine);
            }
        }

        System.out.println(url);

        return url.toString();
    }


    // =====================================================
    // CITATIONS -> JSON
    // =====================================================

    public String toJson(
            List<CitationDto> citations
    ) {

        return jsonMapper.writeValueAsString(
                citations
        );
    }


    // =====================================================
    // JSON -> CITATIONS
    // =====================================================

    public List<CitationDto> fromJson(
            String json
    ) {

        try {

            return jsonMapper.readValue(
                    json,
                    jsonMapper
                            .getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    CitationDto.class
                            )
            );

        } catch (Exception e) {

            return List.of();
        }
    }


    // =====================================================
    // STRING VALUE
    // =====================================================

    private String stringVal(
            Object value
    ) {

        return value == null
                ? ""
                : value.toString();
    }


    // =====================================================
    // INTEGER VALUE
    // =====================================================

    private Integer intVal(
            Object value
    ) {

        if (value == null) {
            return 0;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {

            return Integer.parseInt(
                    value.toString()
            );

        } catch (NumberFormatException e) {

            return 0;
        }
    }
}