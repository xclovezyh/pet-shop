package com.petshop.service;

import com.petshop.dto.common.PageResponse;
import com.petshop.dto.reference.AdminRegionCityResponse;
import com.petshop.dto.reference.AdminRegionDistrictResponse;
import com.petshop.dto.reference.AdminRegionProvinceResponse;
import com.petshop.dto.reference.ReferenceDataResponse;
import com.petshop.dto.reference.RegionAreaResponse;
import com.petshop.dto.reference.RegionCityResponse;
import com.petshop.dto.reference.RegionTreeResponse;
import com.petshop.model.ReferenceOption;
import com.petshop.model.RegionArea;
import com.petshop.repository.ReferenceOptionRepository;
import com.petshop.repository.RegionAreaRepository;
import com.petshop.support.PageSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReferenceDataService {
    private static final String LEVEL_PROVINCE = "province";
    private static final String LEVEL_CITY = "city";
    private static final String LEVEL_DISTRICT = "district";

    private final ReferenceOptionRepository options;
    private final RegionAreaRepository regions;

    public ReferenceDataService(ReferenceOptionRepository options, RegionAreaRepository regions) {
        this.options = options;
        this.regions = regions;
    }

    public ReferenceDataResponse all() {
        ReferenceDataResponse response = new ReferenceDataResponse();
        response.setRegions(regions());
        response.setPostTypes(labels("post_type"));
        response.setPetStatuses(labels("pet_status"));
        response.setPetGenders(labels("pet_gender"));
        response.setAgeRanges(labels("age_range"));
        response.setHealthRecords(labels("health_record"));
        response.setPersonalityTags(labels("personality_tag"));
        response.setServiceTags(labels("service_tag"));
        return response;
    }

    public PageResponse<RegionAreaResponse> regionAdminList(String admin, Integer page, Integer size) {
        int safePage = PageSupport.normalizePage(page);
        int safeSize = PageSupport.normalizeSize(size);
        Page<RegionArea> pageResult = regions.findAdminPage(PageRequest.of(safePage - 1, safeSize));
        return PageSupport.fromPage(pageResult, safePage, safeSize, this::toResponse);
    }

    public List<RegionAreaResponse> regionAdminList(String admin) {
        return regionAdminList(admin, 1, 50).getItems();
    }

    public List<AdminRegionProvinceResponse> regionAdminTree(String admin) {
        List<RegionArea> sorted = sortedRegions();
        Map<Long, List<RegionArea>> byParent = childrenByParent(sorted);
        List<RegionArea> provinces = rootRegions(sorted);
        return provinces.stream()
                .map(province -> toAdminProvince(province, byParent))
                .collect(Collectors.toList());
    }

    private List<RegionTreeResponse> regions() {
        List<RegionArea> sorted = sortedRegions();
        Map<Long, List<RegionArea>> byParent = childrenByParent(sorted);
        return rootRegions(sorted).stream()
                .map(province -> toProvinceResponse(province, byParent))
                .collect(Collectors.toList());
    }

    private Map<Long, List<RegionArea>> childrenByParent(List<RegionArea> sorted) {
        return sorted.stream()
                .filter(region -> region.getParentId() != null)
                .collect(Collectors.groupingBy(RegionArea::getParentId, LinkedHashMap::new, Collectors.toList()));
    }

    private List<RegionArea> rootRegions(List<RegionArea> sorted) {
        return sorted.stream()
                .filter(region -> region.getParentId() == null)
                .collect(Collectors.toList());
    }

    private RegionTreeResponse toProvinceResponse(RegionArea province, Map<Long, List<RegionArea>> byParent) {
        RegionTreeResponse response = new RegionTreeResponse();
        response.setName(province.getName());
        response.setCities(byParent.getOrDefault(province.getId(), new ArrayList<>()).stream()
                .map(city -> toCityResponse(city, byParent))
                .collect(Collectors.toList()));
        return response;
    }

    private RegionCityResponse toCityResponse(RegionArea city, Map<Long, List<RegionArea>> byParent) {
        RegionCityResponse response = new RegionCityResponse();
        response.setName(city.getName());
        response.setDistricts(byParent.getOrDefault(city.getId(), new ArrayList<>()).stream()
                .map(RegionArea::getName)
                .collect(Collectors.toList()));
        return response;
    }

    private AdminRegionProvinceResponse toAdminProvince(RegionArea province, Map<Long, List<RegionArea>> byParent) {
        List<AdminRegionCityResponse> cities = byParent.getOrDefault(province.getId(), new ArrayList<>()).stream()
                .map(city -> toAdminCity(city, byParent))
                .collect(Collectors.toList());

        AdminRegionProvinceResponse response = new AdminRegionProvinceResponse();
        response.setId(province.getId());
        response.setName(province.getName());
        response.setAreaCode(province.getAreaCode());
        response.setCityCount(cities.size());
        response.setDistrictCount(cities.stream().mapToInt(AdminRegionCityResponse::getDistrictCount).sum());
        response.setCities(cities);
        return response;
    }

    private AdminRegionCityResponse toAdminCity(RegionArea city, Map<Long, List<RegionArea>> byParent) {
        List<AdminRegionDistrictResponse> districts = byParent.getOrDefault(city.getId(), new ArrayList<>()).stream()
                .map(this::toAdminDistrict)
                .collect(Collectors.toList());

        AdminRegionCityResponse response = new AdminRegionCityResponse();
        response.setId(city.getId());
        response.setName(city.getName());
        response.setAreaCode(city.getAreaCode());
        response.setDistrictCount(districts.size());
        response.setDistricts(districts);
        return response;
    }

    private AdminRegionDistrictResponse toAdminDistrict(RegionArea district) {
        AdminRegionDistrictResponse response = new AdminRegionDistrictResponse();
        response.setId(district.getId());
        response.setName(district.getName());
        response.setAreaCode(district.getAreaCode());
        return response;
    }

    private List<String> labels(String optionType) {
        return options.findByOptionTypeOrderBySortOrderAscIdAsc(optionType).stream()
                .map(ReferenceOption::getLabel)
                .collect(Collectors.toList());
    }

    private RegionAreaResponse toResponse(RegionArea region) {
        RegionAreaResponse response = new RegionAreaResponse();
        response.setId(region.getId());
        response.setName(region.getName());
        response.setAreaCode(region.getAreaCode());
        response.setLevel(region.getLevel());
        response.setParentId(region.getParentId());
        response.setSortOrder(region.getSortOrder());
        return response;
    }

    private List<RegionArea> sortedRegions() {
        return regions.findAll().stream()
                .sorted(Comparator
                        .comparing(this::levelOrder)
                        .thenComparing(this::safeSort)
                        .thenComparing(region -> safeText(region.getAreaCode()))
                        .thenComparing(RegionArea::getId))
                .collect(Collectors.toList());
    }

    private int safeSort(RegionArea region) {
        return region.getSortOrder() == null ? 0 : region.getSortOrder();
    }

    private int levelOrder(RegionArea region) {
        String level = region.getLevel();
        if (LEVEL_PROVINCE.equals(level)) {
            return 1;
        }
        if (LEVEL_CITY.equals(level)) {
            return 2;
        }
        if (LEVEL_DISTRICT.equals(level)) {
            return 3;
        }
        return 4;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
