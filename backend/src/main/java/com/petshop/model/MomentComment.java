package com.petshop.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class MomentComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 评论ID。 */
    private Long id;

    /** 所属动态ID。 */
    private Long momentId;
    /** 评论人昵称。 */
    private String author;
    /** 评论人用户ID，关联 app_user.id。 */
    private Long authorUserId;
    /** 评论时间。 */
    private LocalDateTime createdAt;

    /** 评论内容。 */
    @Column(length = 500)
    private String content;
}
