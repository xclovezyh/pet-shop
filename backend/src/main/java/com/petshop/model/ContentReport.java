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
@Table(name = "content_reports")
public class ContentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 举报ID。 */
    private Long id;

    /** 被举报内容类型：post 或 moment。 */
    private String targetType;
    /** 被举报内容ID。 */
    private Long targetId;
    /** 举报人昵称。 */
    private String reporter;
    /** 举报状态。 */
    private String status;
    /** 处理人昵称。 */
    private String handledBy;
    /** 举报时间。 */
    private LocalDateTime createdAt;
    /** 处理时间。 */
    private LocalDateTime handledAt;

    /** 举报原因。 */
    @Column(length = 500)
    private String reason;

    /** 处理备注。 */
    @Column(length = 500)
    private String handleNote;
}
