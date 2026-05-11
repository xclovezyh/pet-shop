package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.pet.PetResponse;
import com.petshop.service.PetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public ApiResponse<List<PetResponse>> list() {
        return ApiResponse.success(petService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<PetResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(petService.detail(id));
    }
}
