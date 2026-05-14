package com.petshop.service;

import com.petshop.model.ReferenceOption;
import com.petshop.model.RegionArea;
import com.petshop.dto.common.PageResponse;
import com.petshop.dto.reference.RegionAreaResponse;
import com.petshop.repository.ReferenceOptionRepository;
import com.petshop.repository.RegionAreaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceDataServiceTest {
    @Mock
    private ReferenceOptionRepository options;
    @Mock
    private RegionAreaRepository regions;

    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Test
    void regionAdminListShouldUseDatabasePagination() {
        RegionArea area = new RegionArea();
        area.setId(7L);
        area.setName("上海市");
        area.setAreaCode("310000");
        area.setLevel("province");
        area.setSortOrder(1);

        when(regions.findAdminPage(any(Pageable.class))).thenReturn(new PageImpl<>(
                Collections.singletonList(area),
                PageRequest.of(0, 1),
                2));

        PageResponse<RegionAreaResponse> response = referenceDataService.regionAdminList("admin", 1, 1);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getId()).isEqualTo(7L);
        assertThat(response.getTotal()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }
}
