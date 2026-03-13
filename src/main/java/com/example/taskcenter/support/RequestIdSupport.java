package com.example.taskcenter.support;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public final class RequestIdSupport {
    public static final String REQUEST_ID_ATTR = "requestId";

    private RequestIdSupport() {
    }

    public static String getOrCreate(HttpServletRequest request) {
        Object requestId = request.getAttribute(REQUEST_ID_ATTR);
        if (requestId instanceof String id && !id.isBlank()) {
            return id;
        }

        String newId = "req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        request.setAttribute(REQUEST_ID_ATTR, newId);
        return newId;
    }
}
