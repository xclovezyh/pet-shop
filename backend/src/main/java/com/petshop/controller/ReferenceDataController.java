package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.reference.ReferenceDataResponse;
import com.petshop.dto.reference.RegionAreaResponse;
import com.petshop.model.AppUser;
import com.petshop.service.ReferenceDataService;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
import org.springframework.web.bind.annotation.GetMapping;
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
}
