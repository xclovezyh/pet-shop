package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.message.MessageItemResponse;
import com.petshop.dto.message.MessageSendRequest;
import com.petshop.dto.message.MessageStartRequest;
import com.petshop.dto.message.MessageThreadResponse;
import com.petshop.service.PrivateMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class PrivateMessageController {
    private final PrivateMessageService messageService;

    public PrivateMessageController(PrivateMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<List<MessageThreadResponse>> list(@RequestParam String user) {
        return ApiResponse.success(messageService.list(user));
    }

    @GetMapping("/{id}")
    public ApiResponse<MessageThreadResponse> detail(@PathVariable Long id, @RequestParam String user) {
        return ApiResponse.success(messageService.detail(id, user));
    }

    @PostMapping("/start")
    public ApiResponse<MessageThreadResponse> start(@RequestBody MessageStartRequest request) {
        return ApiResponse.success("私信会话已创建", messageService.start(request));
    }

    @PostMapping("/{id}")
    public ApiResponse<MessageItemResponse> send(@PathVariable Long id, @RequestBody MessageSendRequest request) {
        return ApiResponse.success("私信已发送", messageService.send(id, request));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<MessageThreadResponse> markRead(@PathVariable Long id, @RequestParam String user) {
        return ApiResponse.success("未读状态已更新", messageService.markRead(id, user));
    }
}
