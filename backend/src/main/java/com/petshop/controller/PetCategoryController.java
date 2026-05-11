package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.category.PetCategoryRequest;
import com.petshop.dto.category.PetCategoryResponse;
import com.petshop.service.PetCategoryService;
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
@RequestMapping("/categories")
public class PetCategoryController {
    private final PetCategoryService petCategoryService;

    public PetCategoryController(PetCategoryService petCategoryService) {
        this.petCategoryService = petCategoryService;
    }

    @GetMapping
    public ApiResponse<List<PetCategoryResponse>> list() {
        return ApiResponse.success(petCategoryService.list());
    }

    @PostMapping
    public ApiResponse<PetCategoryResponse> create(@RequestParam String admin, @RequestBody PetCategoryRequest request) {
        return ApiResponse.success("分类已创建", petCategoryService.create(admin, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PetCategoryResponse> update(@PathVariable Long id,
                                                   @RequestParam String admin,
                                                   @RequestBody PetCategoryRequest request) {
        return ApiResponse.success("分类已更新", petCategoryService.update(id, admin, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam String admin) {
        petCategoryService.delete(id, admin);
        return ApiResponse.success("分类已删除", null);
    }
}
