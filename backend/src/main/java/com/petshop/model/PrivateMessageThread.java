package com.petshop.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "private_message_threads")
public class PrivateMessageThread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private String postTitle;
    private String starter;
    private String recipient;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private String peer;

    @Transient
    private Long unreadCount = 0L;

    @Transient
    private List<PrivateMessage> messages = new ArrayList<>();
}
