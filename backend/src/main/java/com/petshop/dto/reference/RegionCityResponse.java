package com.petshop.dto.reference;

import java.util.List;

public class RegionCityResponse {
    private String name;
    private List<String> districts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDistricts() {
        return districts;
    }

    public void setDistricts(List<String> districts) {
        this.districts = districts;
    }
}
