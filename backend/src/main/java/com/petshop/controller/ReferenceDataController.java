package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.reference.ReferenceDataResponse;
import com.petshop.dto.reference.RegionAreaRequest;
import com.petshop.dto.reference.RegionAreaResponse;
import com.petshop.model.AppUser;
import com.petshop.service.ReferenceDataService;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ApiResponse<List<RegionAreaResponse>> regionAdminList(@CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success(referenceDataService.regionAdminList(user.getNickname()));
    }

    @PostMapping("/regions")
    public ApiResponse<RegionAreaResponse> createRegion(@CurrentUser AppUser currentUser,
                                                        @RequestBody RegionAreaRequest request) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success("地区已创建", referenceDataService.createRegion(user.getNickname(), request));
    }

    @PutMapping("/regions/{id}")
    public ApiResponse<RegionAreaResponse> updateRegion(@PathVariable Long id,
                                                        @CurrentUser AppUser currentUser,
                                                        @RequestBody RegionAreaRequest request) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        return ApiResponse.success("地区已更新", referenceDataService.updateRegion(id, user.getNickname(), request));
    }

    @DeleteMapping("/regions/{id}")
    public ApiResponse<Void> deleteRegion(@PathVariable Long id, @CurrentUser AppUser currentUser) {
        AppUser user = UserGuard.requireSuperAdmin(currentUser);
        referenceDataService.deleteRegion(id, user.getNickname());
        return ApiResponse.success("地区已删除", null);
    }
}
