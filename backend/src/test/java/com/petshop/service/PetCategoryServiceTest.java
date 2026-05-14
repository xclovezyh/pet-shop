package com.petshop.service;

import com.petshop.model.PetCategory;
import com.petshop.dto.category.PetCategoryResponse;
import com.petshop.dto.common.PageResponse;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.PetCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetCategoryServiceTest {
    @Mock
    private PetCategoryRepository repository;
    @Mock
    private AppUserRepository users;

    @InjectMocks
    private PetCategoryService petCategoryService;

    @Test
    void adminListShouldUseDatabasePagination() {
        PetCategory category = new PetCategory();
        category.setId(4L);
        category.setName("猫咪");

        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                Collections.singletonList(category),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "id")),
                2));

        PageResponse<PetCategoryResponse> response = petCategoryService.adminList(1, 1);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getId()).isEqualTo(4L);
        assertThat(response.getTotal()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }
}
