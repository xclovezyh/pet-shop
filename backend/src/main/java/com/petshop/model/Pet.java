package com.petshop.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import javax.persistence.Column;

@Getter
@Setter
@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    /** 宠物资料ID。 */
    private Long id;

    /** 宠物名称。 */
    private String name;
    /** 所属分类。 */
    private String category;
    /** 品种。 */
    private String breed;
    /** 年龄描述。 */
    private String age;
    /** 所在城市名称。 */
    private String city;
    /** 所在城市行政区划代码。 */
    private String cityCode;
    /** 资料状态。 */
    private String status;
    /** 参考价格。 */
    private BigDecimal price;
    /** 主图地址。 */
    private String imageUrl;
    /** 图片地址列表，逗号分隔。 */
    @Column(length = 2000)
    private String imageUrls;
    /** 健康摘要。 */
    private String healthInfo;
    /** 健康记录。 */
    @Column(length = 1000)
    private String healthRecords;
    /** 性格特点。 */
    private String personality;
    /** 资料维护人或主人名称。 */
    private String ownerName;
    /** 性别。 */
    private String gender;
    /** 年龄阶段。 */
    private String ageRange;
    /** 是否已免疫。 */
    private Boolean vaccinated;
    /** 是否已驱虫。 */
    private Boolean dewormed;
    /** 是否已绝育。 */
    private Boolean neutered;
    /** 饲养注意事项。 */
    @Column(length = 600)
    private String careNotes;
}
