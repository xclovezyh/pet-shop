package com.petshop.service;

import com.petshop.dto.admin.AdminActionLogResponse;
import com.petshop.model.AdminActionLog;
import com.petshop.repository.AdminActionLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminActionLogServiceTest {
    @Mock
    private AdminActionLogRepository logs;

    @InjectMocks
    private AdminActionLogService service;

    @Test
    void recordShouldPersistAdminAction() {
        when(logs.save(any(AdminActionLog.class))).thenAnswer(invocation -> {
            AdminActionLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        AdminActionLogResponse response = service.record("admin", "USER_PASSWORD_RESET", "USER", 9L, "reset password");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAdminUsername()).isEqualTo("admin");
        assertThat(response.getAction()).isEqualTo("USER_PASSWORD_RESET");
        assertThat(response.getTargetType()).isEqualTo("USER");
        assertThat(response.getTargetId()).isEqualTo(9L);
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void listShouldReturnNewestFirstPage() {
        AdminActionLog first = new AdminActionLog();
        first.setId(2L);
        first.setAdminUsername("admin");
        first.setAction("REPORT_HANDLE");
        first.setTargetType("REPORT");
        first.setTargetId(8L);

        when(logs.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(first));

        assertThat(service.list(1, 10).getItems())
                .extracting(AdminActionLogResponse::getAction)
                .containsExactly("REPORT_HANDLE");
    }
}
