package com.petshop.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "private_messages")
public class PrivateMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 消息ID。 */
    private Long id;

    /** 所属会话ID。 */
    private Long threadId;
    /** 发送人昵称。 */
    private String sender;
    /** 发送人用户ID，关联 app_user.id。 */
    private Long senderUserId;
    /** 接收方是否已读。 */
    private Boolean readByRecipient;
    /** 发送时间。 */
    private LocalDateTime createdAt;

    /** 消息内容。 */
    @Column(length = 1000)
    private String content;
}
