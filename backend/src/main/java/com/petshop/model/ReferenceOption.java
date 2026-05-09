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
public class ReferenceOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 配置项ID。 */
    private Long id;

    /** 配置类型。 */
    private String optionType;
    /** 展示名称。 */
    private String label;
    /** 排序值。 */
    private Integer sortOrder;
}
