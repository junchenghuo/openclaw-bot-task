package com.example.taskcenter.support;

import org.springframework.stereotype.Component;

@Component("displayText")
public class DisplayTextService {

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
}
