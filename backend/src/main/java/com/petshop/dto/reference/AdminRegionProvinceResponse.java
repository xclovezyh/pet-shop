package com.petshop.dto.reference;

import java.util.List;

public class AdminRegionProvinceResponse {
    private Long id;
    private String name;
    private String areaCode;
    private Integer cityCount;
    private Integer districtCount;
    private List<AdminRegionCityResponse> cities;

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

    public Integer getCityCount() {
        return cityCount;
    }

    public void setCityCount(Integer cityCount) {
        this.cityCount = cityCount;
    }

    public Integer getDistrictCount() {
        return districtCount;
    }

    public void setDistrictCount(Integer districtCount) {
        this.districtCount = districtCount;
    }

    public List<AdminRegionCityResponse> getCities() {
        return cities;
    }

    public void setCities(List<AdminRegionCityResponse> cities) {
        this.cities = cities;
    }
}
