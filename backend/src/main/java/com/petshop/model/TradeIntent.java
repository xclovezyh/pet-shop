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
    private Long id;

    private Long postId;
    private String postTitle;
    private String requester;
    private String owner;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(length = 600)
    private String message;

    @Transient
    private MarketPost post;
}
