package com.petshop.dto.message;

import java.time.LocalDateTime;

public class MessageItemResponse {
    private Long id;
    private Long threadId;
    private String sender;
    private String content;
    private Boolean readByRecipient;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getReadByRecipient() {
        return readByRecipient;
    }

    public void setReadByRecipient(Boolean readByRecipient) {
        this.readByRecipient = readByRecipient;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
