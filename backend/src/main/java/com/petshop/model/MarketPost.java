package com.petshop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class MarketPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type;
    private String category;
    private String city;
    private String imageUrl;
    @Column(length = 2000)
    private String imageUrls;
    private String contact;
    private String author;
    private String status;
    private String auditStatus;
    private BigDecimal price;
    private LocalDateTime createdAt;

    @Column(length = 1000)
    private String description;
}
