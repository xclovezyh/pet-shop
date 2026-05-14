package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.category.PetCategoryRequest;
import com.petshop.dto.category.PetCategoryResponse;
import com.petshop.dto.common.PageResponse;
import com.petshop.model.PetCategory;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.PetCategoryRepository;
import com.petshop.support.PageSupport;
import com.petshop.support.UserGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetCategoryService {
    private final PetCategoryRepository repository;
    private final AppUserRepository users;

    public PetCategoryService(PetCategoryRepository repository, AppUserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    public List<PetCategoryResponse> list() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PageResponse<PetCategoryResponse> adminList(Integer page, Integer size) {
        int safePage = PageSupport.normalizePage(page);
        int safeSize = PageSupport.normalizeSize(size);
        Page<PetCategory> pageResult = repository.findAll(PageRequest.of(
                safePage - 1,
                safeSize,
                Sort.by(Sort.Direction.ASC, "id")));
        return PageSupport.fromPage(pageResult, safePage, safeSize, this::toResponse);
    }

    public PetCategoryResponse create(String admin, PetCategoryRequest request) {
        validate(request);
        PetCategory category = new PetCategory();
        copyFields(category, request);
        return toResponse(repository.save(category));
    }

    public PetCategoryResponse update(Long id, String admin, PetCategoryRequest request) {
        PetCategory existing = repository.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.CATEGORY_NOT_FOUND));
        validate(request);
        copyFields(existing, request);
        return toResponse(repository.save(existing));
    }

    public void delete(Long id, String admin) {
        PetCategory existing = repository.findById(id)
                .orElseThrow(() -> new ApiException(ApiErrorCode.CATEGORY_NOT_FOUND));
        repository.delete(existing);
    }

    private void validate(PetCategoryRequest request) {
        if (request == null) {
            throw new ApiException(ApiErrorCode.CATEGORY_PAYLOAD_REQUIRED);
        }
        if (safe(request.getName()).isEmpty()) {
            throw new ApiException(ApiErrorCode.CATEGORY_NAME_REQUIRED);
        }
    }

    private void copyFields(PetCategory category, PetCategoryRequest request) {
        category.setName(request.getName().trim());
        category.setDescription(safe(request.getDescription()));
        category.setImageUrl(safe(request.getImageUrl()));
        category.setTags(safe(request.getTags()));
    }

    private PetCategoryResponse toResponse(PetCategory category) {
        PetCategoryResponse response = new PetCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setImageUrl(category.getImageUrl());
        response.setTags(category.getTags());
        return response;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
