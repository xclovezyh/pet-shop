package com.petshop.service;

import com.petshop.dto.admin.AdminActionLogResponse;
import com.petshop.dto.common.PageResponse;
import com.petshop.model.AdminActionLog;
import com.petshop.repository.AdminActionLogRepository;
import com.petshop.support.PageSupport;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminActionLogService {
    private final AdminActionLogRepository logs;

    public AdminActionLogService(AdminActionLogRepository logs) {
        this.logs = logs;
    }

    public AdminActionLogResponse record(String adminUsername, String action, String targetType, Long targetId, String detail) {
        AdminActionLog log = new AdminActionLog();
        log.setAdminUsername(safe(adminUsername));
        log.setAction(safe(action));
        log.setTargetType(safe(targetType));
        log.setTargetId(targetId);
        log.setDetail(safe(detail));
        log.setCreatedAt(LocalDateTime.now());
        return toResponse(logs.save(log));
    }

    public PageResponse<AdminActionLogResponse> list(Integer page, Integer size) {
        return PageSupport.slice(logs.findAllByOrderByCreatedAtDesc(), page, size, this::toResponse);
    }

    private AdminActionLogResponse toResponse(AdminActionLog log) {
        AdminActionLogResponse response = new AdminActionLogResponse();
        response.setId(log.getId());
        response.setAdminUsername(log.getAdminUsername());
        response.setAction(log.getAction());
        response.setTargetType(log.getTargetType());
        response.setTargetId(log.getTargetId());
        response.setDetail(log.getDetail());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
