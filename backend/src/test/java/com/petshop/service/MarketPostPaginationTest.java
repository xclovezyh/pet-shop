package com.petshop.service;

import com.petshop.model.MarketPost;
import com.petshop.dto.common.PageResponse;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketPostPaginationTest {
    @Mock
    private MarketPostRepository posts;
    @Mock
    private AppUserRepository users;

    @InjectMocks
    private MarketPostService marketPostService;

    @Test
    void adminListShouldUseDatabasePagination() {
        MarketPost post = new MarketPost();
        post.setId(2L);
        post.setTitle("newest");
        post.setCreatedAt(LocalDateTime.of(2026, 5, 4, 12, 0));
        post.setStatus("在售");
        post.setAuditStatus("审核通过");
        post.setPrice(BigDecimal.ZERO);

        when(posts.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                Collections.singletonList(post),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")),
                2));

        PageResponse<MarketPostResponse> response = marketPostService.adminList("admin", 1, 1);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getId()).isEqualTo(2L);
        assertThat(response.getTotal()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }
}
