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
        return switch (code) {
            case "PENDING" -> "待处理";
            case "RUNNING" -> "进行中";
            case "BLOCKED" -> "阻塞";
            case "COMPLETED" -> "已完成";
            case "FAILED" -> "失败";
            case "CANCELLED" -> "已取消";
            default -> code;
        };
    }

    public String projectStatus(String status) {
        if (status == null || status.isBlank()) {
            return "-";
        }
        return switch (status) {
            case "ACTIVE" -> "启用中";
            case "INACTIVE" -> "未启用";
            case "ARCHIVED" -> "已归档";
            default -> status;
        };
    }

    public String taskStatusClass(Object status) {
        if (status == null) {
            return "status-neutral";
        }
        String code = status.toString();
        return switch (code) {
            case "RUNNING", "COMPLETED" -> "status-ok";
            case "FAILED", "CANCELLED" -> "status-fail";
            case "BLOCKED" -> "status-warn";
            case "PENDING" -> "status-pending";
            default -> "status-neutral";
        };
    }

    public String projectStatusClass(String status) {
        if (status == null || status.isBlank()) {
            return "status-neutral";
        }
        return switch (status) {
            case "ACTIVE" -> "status-ok";
            case "ARCHIVED" -> "status-warn";
            case "INACTIVE" -> "status-neutral";
            default -> "status-neutral";
        };
    }

    public String meetingStatus(Object status) {
        if (status == null) {
            return "-";
        }
        return switch (status.toString()) {
            case "VOTING" -> "投票中";
            case "DECIDED" -> "已决策";
            case "CANCELLED" -> "已取消";
            default -> status.toString();
        };
    }

    public String meetingStatusClass(Object status) {
        if (status == null) {
            return "status-neutral";
        }
        return switch (status.toString()) {
            case "VOTING" -> "status-warn";
            case "DECIDED" -> "status-ok";
            case "CANCELLED" -> "status-fail";
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
            case "GENERAL" -> "通用";
            case "PROJECT" -> "项目";
            case "DOCUMENT" -> "文档";
            case "DEVELOPMENT" -> "开发";
            case "TEST" -> "测试";
            case "RESEARCH" -> "调研";
            case "OPERATION" -> "运维";
            case "BUGFIX" -> "缺陷修复";
            default -> value;
        };
    }

    public String taskPriority(Object value) {
        if (value == null) {
            return "-";
        }
        return switch (value.toString()) {
            case "LOW" -> "低";
            case "MEDIUM" -> "中";
            case "HIGH" -> "高";
            case "URGENT" -> "紧急";
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
