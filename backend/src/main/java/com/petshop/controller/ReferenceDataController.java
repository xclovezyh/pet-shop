package com.petshop.controller;

import com.petshop.model.ReferenceOption;
import com.petshop.model.RegionArea;
import com.petshop.repository.ReferenceOptionRepository;
import com.petshop.repository.RegionAreaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/regions/admin")
    public List<RegionArea> regionAdminList() {
        return regions.findAll().stream()
                .sorted((left, right) -> {
                    int level = levelOrder(left.getLevel()) - levelOrder(right.getLevel());
                    if (level != 0) return level;
                    int sort = safeSort(left) - safeSort(right);
                    if (sort != 0) return sort;
                    return left.getId().compareTo(right.getId());
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/regions")
    public RegionArea createRegion(@RequestBody RegionArea region) {
        validateRegion(region);
        region.setId(null);
        if (region.getSortOrder() == null) {
            region.setSortOrder(nextRegionSort(region.getLevel(), region.getParentId()));
        }
        return regions.save(region);
    }

    @PutMapping("/regions/{id}")
    public RegionArea updateRegion(@PathVariable Long id, @RequestBody RegionArea payload) {
        RegionArea region = regions.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "地区不存在"));
        validateRegion(payload);
        region.setName(payload.getName().trim());
        region.setLevel(payload.getLevel());
        region.setParentId(payload.getParentId());
        region.setSortOrder(payload.getSortOrder() == null ? region.getSortOrder() : payload.getSortOrder());
        return regions.save(region);
    }

    @DeleteMapping("/regions/{id}")
    public void deleteRegion(@PathVariable Long id) {
        RegionArea region = regions.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "地区不存在"));
        deleteRegionTree(region);
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

    private void validateRegion(RegionArea region) {
        if (region == null || region.getName() == null || region.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写地区名称");
        }
        if (!"province".equals(region.getLevel()) && !"city".equals(region.getLevel()) && !"district".equals(region.getLevel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "地区层级只能是 province、city 或 district");
        }
        if (!"province".equals(region.getLevel()) && region.getParentId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "市区必须选择上级地区");
        }
    }

    private void deleteRegionTree(RegionArea region) {
        regions.findByParentIdOrderBySortOrderAscIdAsc(region.getId()).forEach(this::deleteRegionTree);
        regions.delete(region);
    }

    private int nextRegionSort(String level, Long parentId) {
        return (int) regions.findAll().stream()
                .filter(region -> level.equals(region.getLevel()))
                .filter(region -> parentId == null ? region.getParentId() == null : parentId.equals(region.getParentId()))
                .count() + 1;
    }

    private int safeSort(RegionArea region) {
        return region.getSortOrder() == null ? 0 : region.getSortOrder();
    }

    private int levelOrder(String level) {
        if ("province".equals(level)) return 1;
        if ("city".equals(level)) return 2;
        if ("district".equals(level)) return 3;
        return 4;
    }
}
