package com.example.taskcenter.controller;

import com.example.taskcenter.repository.ProjectRepository;
import com.example.taskcenter.repository.TaskRepository;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.username=imac",
        "spring.datasource.password=",
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/task_center",
        "taskcenter.project.base-dir=/tmp/openclaw-task-projects-test",
        "task.snowflake.worker-id=1",
        "task.snowflake.datacenter-id=1"
})
@AutoConfigureMockMvc
class TaskApiControllerTaskCodeTest {

    private static final Path TEST_BASE_DIR = Paths.get("/tmp/openclaw-task-projects-test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    private final Set<String> createdProjectCodes = new HashSet<>();

    @BeforeEach
    void setUp() throws IOException {
        deleteRecursively(TEST_BASE_DIR);
        Files.createDirectories(TEST_BASE_DIR);
    }

    @AfterEach
    void tearDown() throws IOException {
        taskRepository.deleteAll();
        for (String code : createdProjectCodes) {
            projectRepository.findByProjectCode(code).ifPresent(projectRepository::delete);
        }
        createdProjectCodes.clear();
        deleteRecursively(TEST_BASE_DIR);
    }

    @Test
    void createTaskShouldGenerateTSnowflakeCode() throws Exception {
        String projectCode = "TCODE_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        createdProjectCodes.add(projectCode);

        String createProjectPayload = objectMapper.writeValueAsString(Map.of(
                "projectCode", projectCode,
                "projectName", "任务编码测试"
        ));

        String projectBody = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProjectPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long projectId = objectMapper.readTree(projectBody).path("data").path("id").asLong();

        String taskPayload = objectMapper.writeValueAsString(Map.of(
                "projectId", projectId,
                "title", "编码生成验证",
                "taskType", "项目",
                "priority", "高",
                "initiator", "测试",
                "ownerName", "测试员"
        ));

        String taskBody = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode taskJson = objectMapper.readTree(taskBody);
        String taskCode = taskJson.path("data").path("taskCode").asText();

        assertTrue(taskCode.startsWith("T"), "任务编码必须以 T 开头");
        assertFalse(taskCode.contains("-"), "任务编码不应包含旧分隔符 -");
        assertTrue(taskCode.matches("T\\d{10,}"), "任务编码应为 T + 数字雪花ID");
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
