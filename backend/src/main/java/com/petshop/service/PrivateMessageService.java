package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.message.MessageItemResponse;
import com.petshop.dto.message.MessageSendRequest;
import com.petshop.dto.message.MessageStartRequest;
import com.petshop.dto.message.MessageThreadResponse;
import com.petshop.model.MarketPost;
import com.petshop.model.PrivateMessage;
import com.petshop.model.PrivateMessageThread;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.PrivateMessageRepository;
import com.petshop.repository.PrivateMessageThreadRepository;
import com.petshop.support.ContentSafety;
import com.petshop.support.UserGuard;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrivateMessageService {
    private final PrivateMessageThreadRepository threads;
    private final PrivateMessageRepository messages;
    private final MarketPostRepository posts;
    private final AppUserRepository users;

    public PrivateMessageService(PrivateMessageThreadRepository threads, PrivateMessageRepository messages, MarketPostRepository posts, AppUserRepository users) {
        this.threads = threads;
        this.messages = messages;
        this.posts = posts;
        this.users = users;
    }

    public List<MessageThreadResponse> list(String user) {
        requireUser(user);
        return threads.findByStarterOrRecipientOrderByUpdatedAtDesc(user, user).stream()
                .map(thread -> toThreadResponse(thread, user))
                .collect(Collectors.toList());
    }

    public MessageThreadResponse detail(Long id, String user) {
        requireUser(user);
        PrivateMessageThread thread = findThread(id);
        ensureParticipant(thread, user);
        return toThreadResponse(thread, user);
    }

    public MessageThreadResponse start(MessageStartRequest request) {
        if (request == null || request.getPostId() == null) {
            throw new ApiException(ApiErrorCode.MESSAGE_POST_REQUIRED);
        }
        requireUser(request.getSender());
        MarketPost post = posts.findById(request.getPostId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND));
        String recipient = safe(post.getAuthor());
        if (isBlank(recipient)) {
            throw new ApiException(ApiErrorCode.MESSAGE_POST_AUTHOR_EMPTY);
        }
        if (recipient.equals(request.getSender())) {
            throw new ApiException(ApiErrorCode.MESSAGE_SELF_FORBIDDEN);
        }
        String content = isBlank(request.getContent()) ? "你好，我想了解「" + safe(post.getTitle()) + "」。" : request.getContent().trim();
        validateContent(content);

        PrivateMessageThread thread = threads.findFirstByPostIdAndStarterAndRecipient(post.getId(), request.getSender(), recipient)
                .orElseGet(() -> createThread(post, request.getSender(), recipient));
        if (messages.findByThreadIdOrderByCreatedAtAscIdAsc(thread.getId()).isEmpty()) {
            saveMessage(thread, request.getSender(), content, true);
        }
        return toThreadResponse(thread, request.getSender());
    }

    public MessageItemResponse send(Long id, MessageSendRequest request) {
        if (request == null) {
            throw new ApiException(ApiErrorCode.MESSAGE_CONTENT_EMPTY);
        }
        requireUser(request.getSender());
        if (isBlank(request.getContent())) {
            throw new ApiException(ApiErrorCode.MESSAGE_CONTENT_EMPTY);
        }
        validateContent(request.getContent());
        PrivateMessageThread thread = findThread(id);
        ensureParticipant(thread, request.getSender());
        return toMessageResponse(saveMessage(thread, request.getSender(), request.getContent().trim(), false));
    }

    public MessageThreadResponse markRead(Long id, String user) {
        requireUser(user);
        PrivateMessageThread thread = findThread(id);
        ensureParticipant(thread, user);
        List<PrivateMessage> items = messages.findByThreadIdOrderByCreatedAtAscIdAsc(id);
        for (PrivateMessage message : items) {
            if (!user.equals(message.getSender())) {
                message.setReadByRecipient(true);
            }
        }
        messages.saveAll(items);
        return toThreadResponse(thread, user);
    }

    private PrivateMessageThread createThread(MarketPost post, String starter, String recipient) {
        LocalDateTime now = LocalDateTime.now();
        PrivateMessageThread thread = new PrivateMessageThread();
        thread.setPostId(post.getId());
        thread.setPostTitle(post.getTitle());
        thread.setStarter(starter);
        thread.setRecipient(recipient);
        thread.setCreatedAt(now);
        thread.setUpdatedAt(now);
        return threads.save(thread);
    }

    private PrivateMessage saveMessage(PrivateMessageThread thread, String sender, String content, boolean readByRecipient) {
        PrivateMessage message = new PrivateMessage();
        message.setThreadId(thread.getId());
        message.setSender(sender);
        message.setContent(content);
        message.setReadByRecipient(readByRecipient);
        message.setCreatedAt(LocalDateTime.now());
        thread.setUpdatedAt(message.getCreatedAt());
        threads.save(thread);
        return messages.save(message);
    }

    private PrivateMessageThread findThread(Long id) {
        return threads.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.MESSAGE_THREAD_NOT_FOUND));
    }

    private void ensureParticipant(PrivateMessageThread thread, String user) {
        if (!user.equals(thread.getStarter()) && !user.equals(thread.getRecipient())) {
            throw new ApiException(ApiErrorCode.MESSAGE_THREAD_FORBIDDEN);
        }
    }

    private void validateContent(String content) {
        ContentSafety.validate(content);
    }

    private void requireUser(String user) {
        if (isBlank(user)) {
            throw new ApiException(ApiErrorCode.MESSAGE_USER_REQUIRED);
        }
        UserGuard.requireActive(users, user, "使用私信");
    }

    private MessageThreadResponse toThreadResponse(PrivateMessageThread thread, String currentUser) {
        MessageThreadResponse response = new MessageThreadResponse();
        response.setId(thread.getId());
        response.setPostId(thread.getPostId());
        response.setPostTitle(thread.getPostTitle());
        response.setStarter(thread.getStarter());
        response.setRecipient(thread.getRecipient());
        response.setPeer(currentUser.equals(thread.getStarter()) ? thread.getRecipient() : thread.getStarter());
        response.setUnreadCount(messages.countByThreadIdAndSenderNotAndReadByRecipientFalse(thread.getId(), currentUser));
        response.setCreatedAt(thread.getCreatedAt());
        response.setUpdatedAt(thread.getUpdatedAt());
        response.setMessages(messages.findByThreadIdOrderByCreatedAtAscIdAsc(thread.getId()).stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private MessageItemResponse toMessageResponse(PrivateMessage message) {
        MessageItemResponse response = new MessageItemResponse();
        response.setId(message.getId());
        response.setThreadId(message.getThreadId());
        response.setSender(message.getSender());
        response.setContent(message.getContent());
        response.setReadByRecipient(message.getReadByRecipient());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
