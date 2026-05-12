package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.common.PageResponse;
import com.petshop.dto.reference.ReferenceDataResponse;
import com.petshop.dto.reference.RegionAreaRequest;
import com.petshop.dto.reference.RegionAreaResponse;
import com.petshop.dto.reference.RegionCityResponse;
import com.petshop.dto.reference.RegionTreeResponse;
import com.petshop.model.ReferenceOption;
import com.petshop.model.RegionArea;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.ReferenceOptionRepository;
import com.petshop.repository.RegionAreaRepository;
import com.petshop.support.PageSupport;
import com.petshop.support.UserGuard;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReferenceDataService {
    private static final String LEVEL_PROVINCE = "province";
    private static final String LEVEL_CITY = "city";
    private static final String LEVEL_DISTRICT = "district";

    private final ReferenceOptionRepository options;
    private final RegionAreaRepository regions;
    private final AppUserRepository users;

    public ReferenceDataService(ReferenceOptionRepository options, RegionAreaRepository regions, AppUserRepository users) {
        this.options = options;
        this.regions = regions;
        this.users = users;
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
        return PageSupport.slice(regions.findAll().stream()
                .sorted((left, right) -> {
                    int level = levelOrder(left.getLevel()) - levelOrder(right.getLevel());
                    if (level != 0) {
                        return level;
                    }
                    int sort = safeSort(left) - safeSort(right);
                    if (sort != 0) {
                        return sort;
                    }
                    return left.getId().compareTo(right.getId());
                })
                .collect(Collectors.toList()), page, size, this::toResponse);
    }

    public List<RegionAreaResponse> regionAdminList(String admin) {
        return regionAdminList(admin, 1, 50).getItems();
    }

    public RegionAreaResponse createRegion(String admin, RegionAreaRequest request) {
        validateRegion(request);
        RegionArea region = new RegionArea();
        region.setName(request.getName().trim());
        region.setLevel(request.getLevel());
        region.setParentId(request.getParentId());
        region.setSortOrder(request.getSortOrder() == null ? nextRegionSort(request.getLevel(), request.getParentId()) : request.getSortOrder());
        return toResponse(regions.save(region));
    }

    public RegionAreaResponse updateRegion(Long id, String admin, RegionAreaRequest request) {
        RegionArea region = regions.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.REGION_NOT_FOUND));
        validateRegion(request);
        region.setName(request.getName().trim());
        region.setLevel(request.getLevel());
        region.setParentId(request.getParentId());
        region.setSortOrder(request.getSortOrder() == null ? region.getSortOrder() : request.getSortOrder());
        return toResponse(regions.save(region));
    }

    public void deleteRegion(Long id, String admin) {
        RegionArea region = regions.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.REGION_NOT_FOUND));
        deleteRegionTree(region);
    }

    private List<RegionTreeResponse> regions() {
        return regions.findByLevelOrderBySortOrderAscIdAsc(LEVEL_PROVINCE).stream()
                .map(this::toProvinceResponse)
                .collect(Collectors.toList());
    }

    private RegionTreeResponse toProvinceResponse(RegionArea province) {
        RegionTreeResponse response = new RegionTreeResponse();
        response.setName(province.getName());
        response.setCities(regions.findByParentIdOrderBySortOrderAscIdAsc(province.getId()).stream()
                .map(this::toCityResponse)
                .collect(Collectors.toList()));
        return response;
    }

    private RegionCityResponse toCityResponse(RegionArea city) {
        RegionCityResponse response = new RegionCityResponse();
        response.setName(city.getName());
        response.setDistricts(regions.findByParentIdOrderBySortOrderAscIdAsc(city.getId()).stream()
                .map(RegionArea::getName)
                .collect(Collectors.toList()));
        return response;
    }

    private List<String> labels(String optionType) {
        return options.findByOptionTypeOrderBySortOrderAscIdAsc(optionType).stream()
                .map(ReferenceOption::getLabel)
                .collect(Collectors.toList());
    }

    private void validateRegion(RegionAreaRequest request) {
        if (request == null) {
            throw new ApiException(ApiErrorCode.REGION_PAYLOAD_REQUIRED);
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ApiException(ApiErrorCode.REGION_NAME_REQUIRED);
        }
        if (!LEVEL_PROVINCE.equals(request.getLevel())
                && !LEVEL_CITY.equals(request.getLevel())
                && !LEVEL_DISTRICT.equals(request.getLevel())) {
            throw new ApiException(ApiErrorCode.REGION_LEVEL_INVALID);
        }
        if (!LEVEL_PROVINCE.equals(request.getLevel()) && request.getParentId() == null) {
            throw new ApiException(ApiErrorCode.REGION_PARENT_REQUIRED);
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

    private RegionAreaResponse toResponse(RegionArea region) {
        RegionAreaResponse response = new RegionAreaResponse();
        response.setId(region.getId());
        response.setName(region.getName());
        response.setLevel(region.getLevel());
        response.setParentId(region.getParentId());
        response.setSortOrder(region.getSortOrder());
        return response;
    }

    private int safeSort(RegionArea region) {
        return region.getSortOrder() == null ? 0 : region.getSortOrder();
    }

    private int levelOrder(String level) {
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
}
