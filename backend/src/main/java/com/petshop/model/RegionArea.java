package com.petshop.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class RegionArea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 地区 ID。 */
    private Long id;

    /** 地区名称。 */
    private String name;

    /** 行政区划代码。 */
    private String areaCode;

    /** 地区层级：province、city、district。 */
    private String level;

    /** 上级地区 ID。 */
    private Long parentId;

    /** 展示排序值。 */
    private Integer sortOrder;
}
