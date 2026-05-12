package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.message.MessageItemResponse;
import com.petshop.dto.message.MessageSendRequest;
import com.petshop.dto.message.MessageStartRequest;
import com.petshop.dto.message.MessageThreadResponse;
import com.petshop.model.AppUser;
import com.petshop.service.PrivateMessageService;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ApiResponse<List<MessageThreadResponse>> list(@CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "查看私信");
        return ApiResponse.success(messageService.list(user.getNickname()));
    }

    @GetMapping("/{id}")
    public ApiResponse<MessageThreadResponse> detail(@PathVariable Long id, @CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "查看私信");
        return ApiResponse.success(messageService.detail(id, user.getNickname()));
    }

    @PostMapping("/start")
    public ApiResponse<MessageThreadResponse> start(@CurrentUser AppUser currentUser,
                                                    @RequestBody MessageStartRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "发起私信");
        request.setSender(user.getNickname());
        return ApiResponse.success("私信会话已创建", messageService.start(request));
    }

    @PostMapping("/{id}")
    public ApiResponse<MessageItemResponse> send(@PathVariable Long id,
                                                 @CurrentUser AppUser currentUser,
                                                 @RequestBody MessageSendRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "发送私信");
        request.setSender(user.getNickname());
        return ApiResponse.success("私信已发送", messageService.send(id, request));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<MessageThreadResponse> markRead(@PathVariable Long id, @CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "查看私信");
        return ApiResponse.success("未读状态已更新", messageService.markRead(id, user.getNickname()));
    }
}
