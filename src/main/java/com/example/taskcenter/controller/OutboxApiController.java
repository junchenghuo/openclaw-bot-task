package com.example.taskcenter.controller;

import com.example.taskcenter.dto.response.ApiResponse;
import com.example.taskcenter.dto.response.OutboxEventResponse;
import com.example.taskcenter.service.OutboxEventService;
import com.example.taskcenter.support.RequestIdSupport;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/outbox")
public class OutboxApiController {

    private final OutboxEventService outboxEventService;

    public OutboxApiController(OutboxEventService outboxEventService) {
        this.outboxEventService = outboxEventService;
    }

    @Operation(summary = "查询Outbox事件详情")
    @GetMapping("/{id}")
    public ApiResponse<OutboxEventResponse> getOutbox(@PathVariable("id") Long id,
                                                      HttpServletRequest request) {
        return ApiResponse.success(
                OutboxEventResponse.from(outboxEventService.getById(id)),
                RequestIdSupport.getOrCreate(request)
        );
    }

    @Operation(summary = "查询最近Outbox事件")
    @GetMapping
    public ApiResponse<List<OutboxEventResponse>> listOutbox(@RequestParam(value = "limit", defaultValue = "50") int limit,
                                                             HttpServletRequest request) {
        List<OutboxEventResponse> data = outboxEventService.listRecent(limit).stream().map(OutboxEventResponse::from).toList();
        return ApiResponse.success(data, RequestIdSupport.getOrCreate(request));
    }

    @Operation(summary = "重放Outbox事件")
    @PostMapping("/{id}/replay")
    public ApiResponse<OutboxEventResponse> replay(@PathVariable("id") Long id,
                                                   HttpServletRequest request) {
        return ApiResponse.success(
                OutboxEventResponse.from(outboxEventService.replay(id)),
                RequestIdSupport.getOrCreate(request)
        );
    }
}
