package com.petshop.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "trade_intents")
public class TradeIntent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 交易意向ID。 */
    private Long id;

    /** 交易帖ID。 */
    private Long postId;
    /** 交易帖标题。 */
    private String postTitle;
    /** 意向提交人昵称。 */
    private String requester;
    /** 帖子发布人昵称。 */
    private String owner;
    /** 意向状态。 */
    private String status;
    /** 提交时间。 */
    private LocalDateTime createdAt;
    /** 状态更新时间。 */
    private LocalDateTime updatedAt;

    /** 意向说明。 */
    @Column(length = 600)
    private String message;

    /** 接口返回时附带的交易帖详情，不持久化。 */
    @Transient
    private MarketPost post;
}
