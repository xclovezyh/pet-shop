package com.petshop.dto.reference;

import java.util.List;

public class RegionTreeResponse {
    private String name;
    private List<RegionCityResponse> cities;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RegionCityResponse> getCities() {
        return cities;
    }

    public void setCities(List<RegionCityResponse> cities) {
        this.cities = cities;
    }
}
