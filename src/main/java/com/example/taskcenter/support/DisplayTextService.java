package com.example.taskcenter.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("displayText")
public class DisplayTextService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String taskStatus(Object status) {
        if (status == null) {
            return "-";
        }
        String code = status.toString();
        return code;
    }

    public String projectStatus(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        return status;
    }

    public String taskStatusClass(Object status) {
        if (status == null) {
            return "status-neutral";
        }
        String code = status.toString();
        return switch (code) {
            case "进行中", "已完成" -> "status-ok";
            case "失败", "已取消" -> "status-fail";
            case "阻塞" -> "status-warn";
            case "待处理" -> "status-pending";
            default -> "status-neutral";
        };
    }

    public String projectStatusClass(String status) {
        if (status == null || status.isBlank()) {
            return "status-neutral";
        }
        return switch (status) {
            case "启用中" -> "status-ok";
            case "已归档" -> "status-warn";
            case "未启用" -> "status-neutral";
            default -> "status-neutral";
        };
    }

    public String meetingStatus(Object status) {
        if (status == null) {
            return "-";
        }
        return switch (status.toString()) {
            case "投票中", "已决策", "已取消" -> status.toString();
            default -> status.toString();
        };
    }

    public String meetingStatusClass(Object status) {
        if (status == null) {
            return "status-neutral";
        }
        return switch (status.toString()) {
            case "投票中" -> "status-warn";
            case "已决策" -> "status-ok";
            case "已取消" -> "status-fail";
            default -> "status-neutral";
        };
    }

    public String dateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.format(DATE_TIME_FORMATTER);
    }

    public String taskType(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return switch (value) {
            case "通用", "项目", "文档", "开发", "测试", "调研", "运维", "缺陷修复" -> value;
            default -> value;
        };
    }

    public String taskPriority(Object value) {
        if (value == null) {
            return "-";
        }
        return switch (value.toString()) {
            case "低", "中", "高", "紧急" -> value.toString();
            default -> value.toString();
        };
    }

    public String prettyJson(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        try {
            Object node = OBJECT_MAPPER.readValue(value, Object.class);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception ex) {
            return value;
        }
    }

    public Map<String, Object> meetingMinutesView(String value) {
        Map<String, Object> result = new HashMap<>();
        result.put("parsed", false);
        result.put("raw", value == null || value.isBlank() ? "-" : value);
        result.put("closedBy", "-");
        result.put("closedAt", "-");
        result.put("decisionOption", "-");
        result.put("decisionSummary", "-");
        result.put("voteCount", List.of());
        result.put("votes", List.of());

        if (value == null || value.isBlank()) {
            return result;
        }

        try {
            var root = OBJECT_MAPPER.readTree(value);
            result.put("parsed", true);
            result.put("closedBy", textOrDash(root.path("closedBy").asText(null)));
            result.put("closedAt", formatDateTimeText(root.path("closedAt").asText(null)));
            result.put("decisionOption", textOrDash(root.path("decisionOption").asText(null)));
            result.put("decisionSummary", textOrDash(root.path("decisionSummary").asText(null)));

            List<Map<String, String>> voteCountList = new ArrayList<>();
            var voteCountNode = root.path("voteCount");
            if (voteCountNode.isObject()) {
                voteCountNode.fields().forEachRemaining(entry -> {
                    Map<String, String> row = new HashMap<>();
                    row.put("option", entry.getKey());
                    row.put("count", String.valueOf(entry.getValue().asLong(0)));
                    voteCountList.add(row);
                });
            }
            result.put("voteCount", voteCountList);

            List<Map<String, String>> votesList = new ArrayList<>();
            var votesNode = root.path("votes");
            if (votesNode.isArray()) {
                for (var voteNode : votesNode) {
                    Map<String, String> row = new HashMap<>();
                    row.put("voter", textOrDash(voteNode.path("voter").asText(null)));
                    row.put("option", textOrDash(voteNode.path("option").asText(null)));
                    row.put("reason", textOrDash(voteNode.path("reason").asText(null)));
                    votesList.add(row);
                }
            }
            result.put("votes", votesList);
            return result;
        } catch (Exception ex) {
            return result;
        }
    }

    private String textOrDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }

    private String formatDateTimeText(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        try {
            return LocalDateTime.parse(value).format(DATE_TIME_FORMATTER);
        } catch (Exception ex) {
            return value;
        }
    }
}
