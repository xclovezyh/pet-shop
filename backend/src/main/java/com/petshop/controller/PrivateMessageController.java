package com.petshop.controller;

import com.petshop.model.MarketPost;
import com.petshop.model.PrivateMessage;
import com.petshop.model.PrivateMessageThread;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.PrivateMessageRepository;
import com.petshop.repository.PrivateMessageThreadRepository;
import com.petshop.support.ContentSafety;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/messages")
public class PrivateMessageController {
    private final PrivateMessageThreadRepository threads;
    private final PrivateMessageRepository messages;
    private final MarketPostRepository posts;

    public PrivateMessageController(PrivateMessageThreadRepository threads, PrivateMessageRepository messages, MarketPostRepository posts) {
        this.threads = threads;
        this.messages = messages;
        this.posts = posts;
    }

    @GetMapping
    public List<PrivateMessageThread> list(@RequestParam String user) {
        requireUser(user);
        return threads.findByStarterOrRecipientOrderByUpdatedAtDesc(user, user).stream()
                .map(thread -> hydrate(thread, user))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PrivateMessageThread detail(@PathVariable Long id, @RequestParam String user) {
        requireUser(user);
        PrivateMessageThread thread = findThread(id);
        ensureParticipant(thread, user);
        return hydrate(thread, user);
    }

    @PostMapping("/start")
    public PrivateMessageThread start(@RequestBody StartRequest request) {
        if (request == null || request.getPostId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择要私信的帖子");
        }
        requireUser(request.getSender());
        MarketPost post = posts.findById(request.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "帖子不存在"));
        String recipient = safe(post.getAuthor());
        if (isBlank(recipient)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "帖子缺少发布者，无法发起私信");
        }
        if (recipient.equals(request.getSender())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能给自己的帖子发私信");
        }
        String content = isBlank(request.getContent()) ? "你好，我想了解「" + safe(post.getTitle()) + "」。" : request.getContent().trim();
        validateContent(content);

        PrivateMessageThread thread = threads.findFirstByPostIdAndStarterAndRecipient(post.getId(), request.getSender(), recipient)
                .orElseGet(() -> createThread(post, request.getSender(), recipient));
        if (messages.findByThreadIdOrderByCreatedAtAscIdAsc(thread.getId()).isEmpty()) {
            saveMessage(thread, request.getSender(), content, true);
        }
        return hydrate(thread, request.getSender());
    }

    @PostMapping("/{id}")
    public PrivateMessage send(@PathVariable Long id, @RequestBody SendRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入私信内容");
        }
        requireUser(request.getSender());
        if (isBlank(request.getContent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入私信内容");
        }
        validateContent(request.getContent());
        PrivateMessageThread thread = findThread(id);
        ensureParticipant(thread, request.getSender());
        return saveMessage(thread, request.getSender(), request.getContent().trim(), false);
    }

    @PutMapping("/{id}/read")
    public PrivateMessageThread markRead(@PathVariable Long id, @RequestParam String user) {
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
        return hydrate(thread, user);
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

    private PrivateMessageThread hydrate(PrivateMessageThread thread, String currentUser) {
        thread.setPeer(currentUser.equals(thread.getStarter()) ? thread.getRecipient() : thread.getStarter());
        thread.setMessages(messages.findByThreadIdOrderByCreatedAtAscIdAsc(thread.getId()));
        thread.setUnreadCount(messages.countByThreadIdAndSenderNotAndReadByRecipientFalse(thread.getId(), currentUser));
        return thread;
    }

    private PrivateMessageThread findThread(Long id) {
        return threads.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "私信会话不存在"));
    }

    private void ensureParticipant(PrivateMessageThread thread, String user) {
        if (!user.equals(thread.getStarter()) && !user.equals(thread.getRecipient())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能查看自己的私信");
        }
    }

    private void validateContent(String content) {
        ContentSafety.validate(content);
    }

    private void requireUser(String user) {
        if (isBlank(user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后再使用私信");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    public static class StartRequest {
        private Long postId;
        private String sender;
        private String content;

        public Long getPostId() {
            return postId;
        }

        public void setPostId(Long postId) {
            this.postId = postId;
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
    }

    public static class SendRequest {
        private String sender;
        private String content;

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
    }
}
