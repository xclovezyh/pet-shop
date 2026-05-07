package com.petshop.controller;

import com.petshop.model.ReferenceOption;
import com.petshop.model.RegionArea;
import com.petshop.repository.ReferenceOptionRepository;
import com.petshop.repository.RegionAreaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reference-data")
public class ReferenceDataController {
    private final ReferenceOptionRepository options;
    private final RegionAreaRepository regions;

    public ReferenceDataController(ReferenceOptionRepository options, RegionAreaRepository regions) {
        this.options = options;
        this.regions = regions;
    }

    @GetMapping
    public Map<String, Object> all() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("regions", regions());
        data.put("postTypes", labels("post_type"));
        data.put("petStatuses", labels("pet_status"));
        data.put("petGenders", labels("pet_gender"));
        data.put("ageRanges", labels("age_range"));
        data.put("healthRecords", labels("health_record"));
        data.put("personalityTags", labels("personality_tag"));
        data.put("serviceTags", labels("service_tag"));
        return data;
    }

    private List<Map<String, Object>> regions() {
        return regions.findByLevelOrderBySortOrderAscIdAsc("province").stream()
                .map(this::province)
                .collect(Collectors.toList());
    }

    private Map<String, Object> province(RegionArea province) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", province.getName());
        data.put("cities", regions.findByParentIdOrderBySortOrderAscIdAsc(province.getId()).stream()
                .map(this::city)
                .collect(Collectors.toList()));
        return data;
    }

    private Map<String, Object> city(RegionArea city) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", city.getName());
        data.put("districts", regions.findByParentIdOrderBySortOrderAscIdAsc(city.getId()).stream()
                .map(RegionArea::getName)
                .collect(Collectors.toList()));
        return data;
    }

    private List<String> labels(String optionType) {
        return options.findByOptionTypeOrderBySortOrderAscIdAsc(optionType).stream()
                .map(ReferenceOption::getLabel)
                .collect(Collectors.toList());
    }
}
