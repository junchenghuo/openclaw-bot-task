package com.example.taskcenter.controller;

import com.example.taskcenter.entity.Project;
import com.example.taskcenter.repository.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.username=imac",
        "spring.datasource.password=",
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/task_center"
})
@AutoConfigureMockMvc
class ProjectMeetingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    private Long projectId;

    @BeforeEach
    void setUp() {
        Project project = projectRepository.findByProjectCode("DAILY_WORK")
                .orElseGet(() -> projectRepository.findAll().stream().findFirst().orElseThrow());
        projectId = project.getId();
    }

    @Test
    void shouldCreateMeetingAndReturnParticipants() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "topic", "框架升级决策",
                "problemStatement", "Vue2 升级路径存在分歧",
                "organizerName", "郑吒（leader）",
                "decisionOptions", new String[]{"渐进迁移", "整体重构"},
                "participants", new Object[]{
                        Map.of("name", "郑吒", "role", "leader", "mention", "@bot-leader", "responsibility", "主持"),
                        Map.of("name", "罗甘道", "role", "fe", "mention", "@bot-fe", "responsibility", "实施")
                }
        ));

        mockMvc.perform(post("/api/projects/{projectId}/meetings", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("VOTING"))
                .andExpect(jsonPath("$.data.participants.length()").value(2));
    }

    @Test
    void shouldVoteAndCloseMeetingWithDecisionMinutes() throws Exception {
        Long meetingId = createMeeting("发布窗口会议");

        String voteA = objectMapper.writeValueAsString(Map.of(
                "voterName", "郑吒（leader）",
                "voterRole", "leader",
                "voterMention", "@bot-leader",
                "optionKey", "本周发布",
                "reason", "业务窗口期"));

        String voteB = objectMapper.writeValueAsString(Map.of(
                "voterName", "罗甘道（fe）",
                "voterRole", "fe",
                "voterMention", "@bot-fe",
                "optionKey", "本周发布",
                "reason", "开发已完成"));

        mockMvc.perform(post("/api/projects/{projectId}/meetings/{meetingId}/votes", projectId, meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(voteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/projects/{projectId}/meetings/{meetingId}/votes", projectId, meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(voteB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        String closePayload = objectMapper.writeValueAsString(Map.of(
                "operatorName", "郑吒（leader）",
                "decisionSummary", "按多数票本周发布，先灰度后全量"
        ));

        String body = mockMvc.perform(post("/api/projects/{projectId}/meetings/{meetingId}/close", projectId, meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DECIDED"))
                .andExpect(jsonPath("$.data.decisionOption").value("本周发布"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        String minutes = json.path("data").path("minutes").asText();
        assertTrue(minutes.contains("decisionOption"));
    }

    @Test
    void shouldRejectCloseWhenNoVotes() throws Exception {
        Long meetingId = createMeeting("无票关闭会议");
        String closePayload = objectMapper.writeValueAsString(Map.of(
                "operatorName", "郑吒（leader）",
                "decisionSummary", "尝试直接关闭"
        ));

        mockMvc.perform(post("/api/projects/{projectId}/meetings/{meetingId}/close", projectId, meetingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_STATUS_TRANSITION"));
    }

    @Test
    void shouldListMeetingsByProject() throws Exception {
        createMeeting("列表查询会议");
        mockMvc.perform(get("/api/projects/{projectId}/meetings", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private Long createMeeting(String topic) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "topic", topic,
                "problemStatement", "关键决策讨论",
                "organizerName", "郑吒（leader）",
                "decisionOptions", new String[]{"本周发布", "下周发布"},
                "participants", new Object[]{
                        Map.of("name", "郑吒", "role", "leader", "mention", "@bot-leader", "responsibility", "主持"),
                        Map.of("name", "罗甘道", "role", "fe", "mention", "@bot-fe", "responsibility", "执行")
                }
        ));

        String body = mockMvc.perform(post("/api/projects/{projectId}/meetings", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(body);
        JsonNode meetingId = json.path("data").path("id");
        assertNotNull(meetingId);
        return meetingId.asLong();
    }
}
