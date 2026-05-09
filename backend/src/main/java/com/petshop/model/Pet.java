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
    private Long id;

    private String name;
    private String category;
    private String breed;
    private String age;
    private String city;
    private String status;
    private BigDecimal price;
    private String imageUrl;
    @Column(length = 2000)
    private String imageUrls;
    private String healthInfo;
    @Column(length = 1000)
    private String healthRecords;
    private String personality;
    private String ownerName;
    private String gender;
    private String ageRange;
    private Boolean vaccinated;
    private Boolean dewormed;
    private Boolean neutered;
    @Column(length = 600)
    private String careNotes;
}
