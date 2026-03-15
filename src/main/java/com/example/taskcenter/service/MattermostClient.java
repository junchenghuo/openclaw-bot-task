package com.example.taskcenter.service;

import com.example.taskcenter.config.MattermostProperties;
import com.example.taskcenter.exception.BusinessException;
import com.example.taskcenter.exception.ErrorCodes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MattermostClient {

    private final MattermostProperties properties;
    private final ObjectMapper objectMapper;

    public MattermostClient(MattermostProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String createPost(String channelId, String message, List<String> fileIds) {
        String token = properties.getBotToken() == null ? "" : properties.getBotToken().trim();
        if (token.isBlank()) {
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Mattermost botToken 未配置", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("channel_id", channelId);
        body.put("message", message);
        if (fileIds != null && !fileIds.isEmpty()) {
            body.put("file_ids", fileIds);
        }

        try {
            HttpResult primary = post(token, "/api/v4/posts", body);
            if (primary.status >= 200 && primary.status < 300) {
                JsonNode json = objectMapper.readTree(primary.body);
                String postId = json.path("id").asText();
                if (postId == null || postId.isBlank()) {
                    throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Mattermost 响应缺少 post_id", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                return postId;
            }

            // 若主账号权限不足，尝试 fallback 账号自动补偿。
            if (primary.status == 403 && primary.body != null && primary.body.contains("create_post")) {
                String fallback = properties.getFallbackBotToken() == null ? "" : properties.getFallbackBotToken().trim();
                if (!fallback.isBlank()) {
                    HttpResult fb = post(fallback, "/api/v4/posts", body);
                    if (fb.status >= 200 && fb.status < 300) {
                        JsonNode json = objectMapper.readTree(fb.body);
                        String postId = json.path("id").asText();
                        if (postId == null || postId.isBlank()) {
                            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Mattermost fallback 响应缺少 post_id", HttpStatus.INTERNAL_SERVER_ERROR);
                        }
                        return postId;
                    }
                    throw new BusinessException(
                            ErrorCodes.INTERNAL_ERROR,
                            "Mattermost 发帖失败（主账号与fallback均失败）: primary=" + primary.status + " " + primary.body +
                                    " | fallback=" + fb.status + " " + fb.body,
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                }
            }

            throw new BusinessException(
                    ErrorCodes.INTERNAL_ERROR,
                    "Mattermost 发帖失败: HTTP " + primary.status + " " + primary.body,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Mattermost 调用异常: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void ensureCanPost(String channelId) {
        String token = properties.getBotToken() == null ? "" : properties.getBotToken().trim();
        if (token.isBlank()) {
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Mattermost botToken 未配置", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Map<String, Object> probe = new HashMap<>();
        probe.put("channel_id", channelId);
        probe.put("message", "[probe] outbox permission check");

        HttpResult res = post(token, "/api/v4/posts", probe);
        if (res.status >= 200 && res.status < 300) {
            return;
        }

        if (res.status == 403 && res.body != null && res.body.contains("create_post")) {
            String fallback = properties.getFallbackBotToken() == null ? "" : properties.getFallbackBotToken().trim();
            if (!fallback.isBlank()) {
                HttpResult fb = post(fallback, "/api/v4/posts", probe);
                if (fb.status >= 200 && fb.status < 300) {
                    return;
                }
                throw new BusinessException(
                        ErrorCodes.INVALID_ARGUMENT,
                        "频道发帖权限校验失败（主账号+fallback）: primary=" + res.status + " " + res.body +
                                " | fallback=" + fb.status + " " + fb.body,
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        throw new BusinessException(
                ErrorCodes.INVALID_ARGUMENT,
                "频道发帖权限校验失败: HTTP " + res.status + " " + res.body,
                HttpStatus.BAD_REQUEST
        );
    }

    private HttpResult post(String token, String path, Map<String, Object> body) {
        try {
            URL url = new URL(properties.getBaseUrl().replaceAll("/$", "") + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            byte[] reqBytes = objectMapper.writeValueAsBytes(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(reqBytes);
            }

            int status = conn.getResponseCode();
            InputStream is = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
            String resp = is == null ? "" : new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return new HttpResult(status, resp);
        } catch (Exception ex) {
            return new HttpResult(500, "{" + "\"message\":\"" + ex.getMessage() + "\"}");
        }
    }

    private record HttpResult(int status, String body) {
    }
}
