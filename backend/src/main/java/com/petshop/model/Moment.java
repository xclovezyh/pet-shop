package com.petshop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Moment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 动态ID。 */
    private Long id;

    /** 发布人昵称。 */
    private String author;
    /** 发布人用户ID，关联 app_user.id。 */
    private Long authorUserId;
    /** 宠物名称。 */
    private String petName;
    /** 动态分类。 */
    private String category;
    /** 发布城市名称。 */
    private String city;
    /** 发布城市行政区划代码。 */
    private String cityCode;
    /** 主图地址。 */
    private String imageUrl;
    /** 审核状态。 */
    private String auditStatus;
    /** 图片地址列表，逗号分隔。 */
    @Column(length = 2000)
    private String imageUrls;
    /** 点赞数。 */
    private Integer likes;
    /** 发布时间。 */
    private LocalDateTime createdAt;

    /** 动态正文。 */
    @Column(length = 1000)
    private String content;
}
