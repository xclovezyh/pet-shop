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
    /** 地区ID。 */
    private Long id;

    /** 地区名称。 */
    private String name;
    /** 地区层级：province、city、district。 */
    private String level;
    /** 上级地区ID。 */
    private Long parentId;
    /** 排序值。 */
    private Integer sortOrder;
}
