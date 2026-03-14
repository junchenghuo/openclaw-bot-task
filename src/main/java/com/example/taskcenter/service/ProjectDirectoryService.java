package com.example.taskcenter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Locale;

@Service
public class ProjectDirectoryService {

    private final Path baseDir;

    public ProjectDirectoryService(
            @Value("${taskcenter.project.base-dir:/Users/imac/midCreate/openclaw-workspaces/ai-team/projects}")
            String baseDir
    ) {
        this.baseDir = Paths.get(baseDir);
    }

    public ProjectPaths createProjectDirectories(String projectCode, String projectName) {
        String folderName = buildFolderName(projectCode, projectName);
        Path root = baseDir.resolve(folderName);
        Path work = root.resolve("work");
        Path memory = root.resolve("memory");
        ensureStandardDirectories(work, memory);

        return new ProjectPaths(work.toString(), memory.toString());
    }

    public void ensureDirectories(String workspacePath, String memoryPath) {
        if (workspacePath == null || workspacePath.isBlank() || memoryPath == null || memoryPath.isBlank()) {
            return;
        }
        ensureStandardDirectories(Paths.get(workspacePath), Paths.get(memoryPath));
    }

    private String buildFolderName(String projectCode, String projectName) {
        String codeSlug = toSlug(projectCode, "project");
        String nameSlug = toSlug(projectName, "workspace");
        return codeSlug + "-" + nameSlug;
    }

    private String toSlug(String input, String fallback) {
        String raw = input == null ? "" : input.trim();
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "");
        String slug = normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-{2,}", "-");
        return slug.isBlank() ? fallback : slug;
    }

    private void ensureStandardDirectories(Path work, Path memory) {
        Path root = work.getParent();
        if (root == null) {
            throw new IllegalStateException("项目目录创建失败: 无法解析项目根目录");
        }
        Path wbs = root.resolve("wbs");
        Path meetings = root.resolve("meetings");
        Path meta = root.resolve("meta");
        try {
            Files.createDirectories(work);
            Files.createDirectories(memory);
            Files.createDirectories(wbs);
            Files.createDirectories(meetings);
            Files.createDirectories(meta);
        } catch (IOException ex) {
            throw new IllegalStateException("项目目录创建失败: " + ex.getMessage());
        }
    }

    public record ProjectPaths(String workspacePath, String memoryPath) {
    }
}
