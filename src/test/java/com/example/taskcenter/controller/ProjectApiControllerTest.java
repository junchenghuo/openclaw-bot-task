package com.example.taskcenter.controller;

import com.example.taskcenter.repository.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.username=imac",
        "spring.datasource.password=",
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/task_center",
        "taskcenter.project.base-dir=/tmp/openclaw-task-projects-test"
})
@AutoConfigureMockMvc
class ProjectApiControllerTest {

    private static final Path TEST_BASE_DIR = Paths.get("/tmp/openclaw-task-projects-test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    private final Set<String> createdProjectCodes = new HashSet<>();

    @BeforeEach
    void setUp() throws IOException {
        deleteRecursively(TEST_BASE_DIR);
        Files.createDirectories(TEST_BASE_DIR);
    }

    @AfterEach
    void tearDown() throws IOException {
        for (String code : createdProjectCodes) {
            projectRepository.findByProjectCode(code).ifPresent(projectRepository::delete);
        }
        createdProjectCodes.clear();
        deleteRecursively(TEST_BASE_DIR);
    }

    @Test
    void shouldCreateProjectWithWorkspaceAndMemoryDirectories() throws Exception {
        String projectCode = "AI_PORTAL_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String payload = objectMapper.writeValueAsString(Map.of(
                "projectCode", projectCode,
                "projectName", "AI 门户改造",
                "description", "项目目录创建验证"
        ));
        createdProjectCodes.add(projectCode);

        String body = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectCode").value(projectCode))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        Long projectId = json.path("data").path("id").asLong();
        String workspacePath = json.path("data").path("workspacePath").asText();
        String memoryPath = json.path("data").path("memoryPath").asText();

        Path workspaceDir = Paths.get(workspacePath);
        Path memoryDir = Paths.get(memoryPath);
        Path rootDir = workspaceDir.getParent();

        assertTrue(Files.isDirectory(rootDir));
        assertTrue(Files.isDirectory(workspaceDir));
        assertTrue(Files.isDirectory(memoryDir));
        assertTrue(Files.isDirectory(rootDir.resolve("wbs")));
        assertTrue(Files.isDirectory(rootDir.resolve("meetings")));
        assertTrue(Files.isDirectory(rootDir.resolve("meta")));

        String detailBody = mockMvc.perform(get("/api/projects/{id}", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode detail = objectMapper.readTree(detailBody);

        assertEquals(workspacePath, detail.path("data").path("workspacePath").asText());
        assertEquals(memoryPath, detail.path("data").path("memoryPath").asText());
    }

    @Test
    void shouldRejectDuplicateProjectCode() throws Exception {
        String projectCode = "AI_PORTAL_DUP_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String payload = objectMapper.writeValueAsString(Map.of(
                "projectCode", projectCode,
                "projectName", "AI 门户一期"
        ));
        createdProjectCodes.add(projectCode);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_ARGUMENT"));
    }

    private void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try (var walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
    }
}
