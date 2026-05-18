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
    /** 交易帖ID。 */
    private Long id;

    /** 帖子标题。 */
    private String title;
    /** 交易类型：售卖、领养、互换、闲置、寄养等。 */
    private String type;
    /** 宠物或用品分类。 */
    private String category;
    /** 交易城市名称。 */
    private String city;
    /** 交易城市行政区划代码。 */
    private String cityCode;
    /** 主图地址。 */
    private String imageUrl;
    /** 图片地址列表，逗号分隔。 */
    @Column(length = 2000)
    private String imageUrls;
    /** 联系渠道，固定为站内私信。 */
    private String contact;
    /** 发布人昵称。 */
    private String author;
    /** 发布人用户ID，关联 app_user.id。 */
    private Long authorUserId;
    /** 交易状态。 */
    private String status;
    /** 审核状态。 */
    private String auditStatus;
    /** 价格或预算。 */
    private BigDecimal price;
    /** 发布时间。 */
    private LocalDateTime createdAt;

    /** 帖子详情。 */
    @Column(length = 1000)
    private String description;
}
