package com.petshop.dto.reference;

import java.util.List;

public class AdminRegionCityResponse {
    private Long id;
    private String name;
    private String areaCode;
    private Integer districtCount;
    private List<AdminRegionDistrictResponse> districts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public Integer getDistrictCount() {
        return districtCount;
    }

    public void setDistrictCount(Integer districtCount) {
        this.districtCount = districtCount;
    }

    public List<AdminRegionDistrictResponse> getDistricts() {
        return districts;
    }

    public void setDistricts(List<AdminRegionDistrictResponse> districts) {
        this.districts = districts;
    }
}
