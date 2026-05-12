package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.report.ContentReportCreateRequest;
import com.petshop.dto.report.ContentReportHandleRequest;
import com.petshop.dto.report.ContentReportResponse;
import com.petshop.model.AppUser;
import com.petshop.model.ContentReport;
import com.petshop.model.MarketPost;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.ContentReportRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.MomentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentReportServiceTest {
    @Mock
    private ContentReportRepository reports;
    @Mock
    private MarketPostRepository posts;
    @Mock
    private MomentRepository moments;
    @Mock
    private AppUserRepository users;

    @InjectMocks
    private ContentReportService contentReportService;

    @Test
    void createShouldRejectUnsupportedTargetType() {
        ContentReportCreateRequest request = new ContentReportCreateRequest();
        request.setReporter("alice");
        request.setTargetType("pet");
        request.setTargetId(1L);
        request.setReason("不合规");

        AppUser reporter = new AppUser();
        reporter.setNickname("alice");
        reporter.setRole("USER");
        reporter.setBlacklisted(false);
        when(users.findByNickname("alice")).thenReturn(Optional.of(reporter));

        assertThatThrownBy(() -> contentReportService.create(request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.REPORT_TARGET_TYPE_UNSUPPORTED);
    }

    @Test
    void handleShouldRemoveTargetAndBlacklistAuthor() {
        ContentReport report = new ContentReport();
        report.setId(8L);
        report.setTargetType("post");
        report.setTargetId(10L);
        report.setReporter("alice");
        report.setReason("违规");
        report.setStatus("待处理");
        report.setCreatedAt(LocalDateTime.now());
        when(reports.findById(8L)).thenReturn(Optional.of(report));
        when(reports.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarketPost post = new MarketPost();
        post.setId(10L);
        post.setAuthor("bob");
        when(posts.findById(10L)).thenReturn(Optional.of(post));
        when(posts.save(any(MarketPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser author = new AppUser();
        author.setNickname("bob");
        author.setRole("USER");
        author.setBlacklisted(false);
        when(users.findByNickname("bob")).thenReturn(Optional.of(author));
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContentReportHandleRequest request = new ContentReportHandleRequest();
        request.setAction("removeAndBlockAuthor");
        request.setNote("重复违规");

        ContentReportResponse response = contentReportService.handle(8L, "admin", request);

        assertThat(response.getHandledBy()).isEqualTo("admin");
        assertThat(response.getStatus()).isNotBlank();
        assertThat(author.getBlacklisted()).isTrue();
        assertThat(author.getBlacklistReason()).isEqualTo("重复违规");
        assertThat(post.getAuditStatus()).isNotBlank();
    }
}
