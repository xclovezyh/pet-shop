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
    /** 会话ID。 */
    private Long id;

    /** 关联交易帖ID。 */
    private Long postId;
    /** 关联交易帖标题。 */
    private String postTitle;
    /** 发起人昵称。 */
    private String starter;
    /** 发起人用户ID，关联 app_user.id。 */
    private Long starterUserId;
    /** 接收人昵称。 */
    private String recipient;
    /** 接收人用户ID，关联 app_user.id。 */
    private Long recipientUserId;
    /** 会话创建时间。 */
    private LocalDateTime createdAt;
    /** 最近消息时间。 */
    private LocalDateTime updatedAt;

    /** 当前登录用户视角下的对方昵称，不持久化。 */
    @Transient
    private String peer;

    /** 当前登录用户未读消息数，不持久化。 */
    @Transient
    private Long unreadCount = 0L;

    /** 会话消息列表，不持久化。 */
    @Transient
    private List<PrivateMessage> messages = new ArrayList<>();
}
