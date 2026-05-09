package com.petshop.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PetCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 分类ID。 */
    private Long id;

    /** 分类名称。 */
    private String name;
    /** 分类说明。 */
    private String description;
    /** 分类封面图片地址。 */
    private String imageUrl;
    /** 分类标签，逗号分隔。 */
    private String tags;
}
