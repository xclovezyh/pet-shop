package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.message.MessageItemResponse;
import com.petshop.dto.message.MessageSendRequest;
import com.petshop.dto.message.MessageStartRequest;
import com.petshop.dto.message.MessageThreadResponse;
import com.petshop.model.AppUser;
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

    public PrivateMessageService(PrivateMessageThreadRepository threads,
                                 PrivateMessageRepository messages,
                                 MarketPostRepository posts,
                                 AppUserRepository users) {
        this.threads = threads;
        this.messages = messages;
        this.posts = posts;
        this.users = users;
    }

    public List<MessageThreadResponse> list(AppUser currentUser) {
        AppUser user = requireUser(currentUser);
        return threads.findByStarterUserIdOrRecipientUserIdOrderByUpdatedAtDesc(user.getId(), user.getId()).stream()
                .map(thread -> toThreadResponse(thread, user))
                .collect(Collectors.toList());
    }

    public MessageThreadResponse detail(Long id, AppUser currentUser) {
        AppUser user = requireUser(currentUser);
        PrivateMessageThread thread = findThread(id);
        ensureParticipant(thread, user);
        return toThreadResponse(thread, user);
    }

    public MessageThreadResponse start(AppUser currentUser, MessageStartRequest request) {
        AppUser user = requireUser(currentUser);
        if (request == null || request.getPostId() == null) {
            throw new ApiException(ApiErrorCode.MESSAGE_POST_REQUIRED);
        }
        MarketPost post = posts.findById(request.getPostId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND));
        String recipient = safe(post.getAuthor());
        Long recipientUserId = post.getAuthorUserId();
        if (isBlank(recipient)) {
            throw new ApiException(ApiErrorCode.MESSAGE_POST_AUTHOR_EMPTY);
        }
        if ((recipientUserId != null && recipientUserId.equals(user.getId())) || recipient.equals(user.getNickname())) {
            throw new ApiException(ApiErrorCode.MESSAGE_SELF_FORBIDDEN);
        }
        String content = isBlank(request.getContent()) ? "你好，我想了解「" + safe(post.getTitle()) + "」。" : request.getContent().trim();
        validateContent(content);

        PrivateMessageThread thread = recipientUserId == null
                ? threads.findFirstByPostIdAndStarterAndRecipient(post.getId(), user.getNickname(), recipient)
                .orElseGet(() -> createThread(post, user, recipient, null))
                : threads.findFirstByPostIdAndStarterUserIdAndRecipientUserId(post.getId(), user.getId(), recipientUserId)
                .orElseGet(() -> createThread(post, user, recipient, recipientUserId));
        if (messages.findByThreadIdOrderByCreatedAtAscIdAsc(thread.getId()).isEmpty()) {
            saveMessage(thread, user, content, true);
        }
        return toThreadResponse(thread, user);
    }

    public MessageItemResponse send(Long id, AppUser currentUser, MessageSendRequest request) {
        AppUser user = requireUser(currentUser);
        if (request == null) {
            throw new ApiException(ApiErrorCode.MESSAGE_CONTENT_EMPTY);
        }
        if (isBlank(request.getContent())) {
            throw new ApiException(ApiErrorCode.MESSAGE_CONTENT_EMPTY);
        }
        validateContent(request.getContent());
        PrivateMessageThread thread = findThread(id);
        ensureParticipant(thread, user);
        return toMessageResponse(saveMessage(thread, user, request.getContent().trim(), false));
    }

    public MessageThreadResponse markRead(Long id, AppUser currentUser) {
        AppUser user = requireUser(currentUser);
        PrivateMessageThread thread = findThread(id);
        ensureParticipant(thread, user);
        List<PrivateMessage> items = messages.findByThreadIdOrderByCreatedAtAscIdAsc(id);
        for (PrivateMessage message : items) {
            if (message.getSenderUserId() == null
                    ? !user.getNickname().equals(message.getSender())
                    : !user.getId().equals(message.getSenderUserId())) {
                message.setReadByRecipient(true);
            }
        }
        messages.saveAll(items);
        return toThreadResponse(thread, user);
    }

    private PrivateMessageThread createThread(MarketPost post, AppUser starter, String recipient, Long recipientUserId) {
        LocalDateTime now = LocalDateTime.now();
        PrivateMessageThread thread = new PrivateMessageThread();
        thread.setPostId(post.getId());
        thread.setPostTitle(post.getTitle());
        thread.setStarter(starter.getNickname());
        thread.setStarterUserId(starter.getId());
        thread.setRecipient(recipient);
        thread.setRecipientUserId(recipientUserId);
        thread.setCreatedAt(now);
        thread.setUpdatedAt(now);
        return threads.save(thread);
    }

    private PrivateMessage saveMessage(PrivateMessageThread thread, AppUser sender, String content, boolean readByRecipient) {
        PrivateMessage message = new PrivateMessage();
        message.setThreadId(thread.getId());
        message.setSender(sender.getNickname());
        message.setSenderUserId(sender.getId());
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

    private void ensureParticipant(PrivateMessageThread thread, AppUser user) {
        if (thread.getStarterUserId() != null || thread.getRecipientUserId() != null) {
            if (user.getId().equals(thread.getStarterUserId()) || user.getId().equals(thread.getRecipientUserId())) {
                return;
            }
            throw new ApiException(ApiErrorCode.MESSAGE_THREAD_FORBIDDEN);
        }
        if (!user.getNickname().equals(thread.getStarter()) && !user.getNickname().equals(thread.getRecipient())) {
            throw new ApiException(ApiErrorCode.MESSAGE_THREAD_FORBIDDEN);
        }
    }

    private void validateContent(String content) {
        ContentSafety.validate(content);
    }

    private AppUser requireUser(AppUser user) {
        if (user == null || user.getId() == null || isBlank(user.getNickname())) {
            throw new ApiException(ApiErrorCode.MESSAGE_USER_REQUIRED);
        }
        return UserGuard.requireAuthenticated(user, "使用私信");
    }

    private MessageThreadResponse toThreadResponse(PrivateMessageThread thread, AppUser currentUser) {
        MessageThreadResponse response = new MessageThreadResponse();
        response.setId(thread.getId());
        response.setPostId(thread.getPostId());
        response.setPostTitle(thread.getPostTitle());
        response.setStarter(thread.getStarter());
        response.setRecipient(thread.getRecipient());
        boolean starter = thread.getStarterUserId() == null
                ? currentUser.getNickname().equals(thread.getStarter())
                : currentUser.getId().equals(thread.getStarterUserId());
        response.setPeer(starter ? thread.getRecipient() : thread.getStarter());
        response.setUnreadCount(thread.getStarterUserId() == null && thread.getRecipientUserId() == null
                ? messages.countByThreadIdAndSenderNotAndReadByRecipientFalse(thread.getId(), currentUser.getNickname())
                : messages.countByThreadIdAndSenderUserIdNotAndReadByRecipientFalse(thread.getId(), currentUser.getId()));
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
        return value == null ? "" : value.trim();
    }
}
