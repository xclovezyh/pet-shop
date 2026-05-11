package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.reference.ReferenceDataResponse;
import com.petshop.dto.reference.RegionAreaRequest;
import com.petshop.dto.reference.RegionAreaResponse;
import com.petshop.service.ReferenceDataService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reference-data")
public class ReferenceDataController {
    private final ReferenceDataService referenceDataService;

    public ReferenceDataController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public ApiResponse<ReferenceDataResponse> all() {
        return ApiResponse.success(referenceDataService.all());
    }

    @GetMapping("/regions/admin")
    public ApiResponse<List<RegionAreaResponse>> regionAdminList(@RequestParam String admin) {
        return ApiResponse.success(referenceDataService.regionAdminList(admin));
    }

    @PostMapping("/regions")
    public ApiResponse<RegionAreaResponse> createRegion(@RequestParam String admin, @RequestBody RegionAreaRequest request) {
        return ApiResponse.success("地区已创建", referenceDataService.createRegion(admin, request));
    }

    @PutMapping("/regions/{id}")
    public ApiResponse<RegionAreaResponse> updateRegion(@PathVariable Long id,
                                                        @RequestParam String admin,
                                                        @RequestBody RegionAreaRequest request) {
        return ApiResponse.success("地区已更新", referenceDataService.updateRegion(id, admin, request));
    }

    @DeleteMapping("/regions/{id}")
    public ApiResponse<Void> deleteRegion(@PathVariable Long id, @RequestParam String admin) {
        referenceDataService.deleteRegion(id, admin);
        return ApiResponse.success("地区已删除", null);
    }
}
