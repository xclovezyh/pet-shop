package com.petshop.service;

import com.petshop.model.Moment;
import com.petshop.dto.common.PageResponse;
import com.petshop.dto.moment.MomentResponse;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MomentCommentRepository;
import com.petshop.repository.MomentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MomentPaginationTest {
    @Mock
    private MomentRepository moments;
    @Mock
    private MomentCommentRepository comments;
    @Mock
    private AppUserRepository users;

    @InjectMocks
    private MomentService momentService;

    @Test
    void adminListShouldUseDatabasePagination() {
        Moment moment = new Moment();
        moment.setId(3L);
        moment.setAuthor("alice");
        moment.setCreatedAt(LocalDateTime.of(2026, 5, 5, 12, 0));
        moment.setAuditStatus("审核通过");

        when(moments.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(
                Collections.singletonList(moment),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createdAt")),
                2));

        PageResponse<MomentResponse> response = momentService.adminList("admin", 1, 1);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getId()).isEqualTo(3L);
        assertThat(response.getTotal()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(2);
    }
}
