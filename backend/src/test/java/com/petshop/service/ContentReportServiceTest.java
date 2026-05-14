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
        request.setReason("invalid");

        AppUser reporter = new AppUser();
        reporter.setNickname("alice");
        reporter.setRole("USER");
        reporter.setBlacklisted(false);

        assertThatThrownBy(() -> contentReportService.create(reporter, request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.REPORT_TARGET_TYPE_UNSUPPORTED);
    }

    @Test
    void myReportsShouldUseReporterUserIdBeforeNickname() {
        AppUser currentUser = new AppUser();
        currentUser.setId(2L);
        currentUser.setNickname("alice");
        currentUser.setRole("USER");
        currentUser.setBlacklisted(false);

        when(reports.findByReporterUserIdOrderByCreatedAtDesc(2L)).thenReturn(java.util.Collections.emptyList());

        assertThat(contentReportService.myReports(currentUser)).isEmpty();
    }

    @Test
    void myReportsShouldIgnoreLegacyNicknameOnlyReportsWhenUserIdsAreMissing() {
        AppUser currentUser = new AppUser();
        currentUser.setId(2L);
        currentUser.setNickname("alice");
        currentUser.setRole("USER");
        currentUser.setBlacklisted(false);

        ContentReport report = new ContentReport();
        report.setId(9L);
        report.setReporter("alice");
        report.setStatus("pending");
        report.setCreatedAt(LocalDateTime.now());

        when(reports.findByReporterUserIdOrderByCreatedAtDesc(2L)).thenReturn(java.util.Collections.emptyList());

        assertThat(contentReportService.myReports(currentUser)).isEmpty();
    }

    @Test
    void handleShouldRemoveTargetAndBlacklistAuthor() {
        ContentReport report = new ContentReport();
        report.setId(8L);
        report.setTargetType("post");
        report.setTargetId(10L);
        report.setReporter("alice");
        report.setReason("violation");
        report.setStatus("pending");
        report.setCreatedAt(LocalDateTime.now());
        when(reports.findById(8L)).thenReturn(Optional.of(report));
        when(reports.save(any(ContentReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarketPost post = new MarketPost();
        post.setId(10L);
        post.setAuthor("bob");
        post.setAuthorUserId(5L);
        when(posts.findById(10L)).thenReturn(Optional.of(post));
        when(posts.save(any(MarketPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser author = new AppUser();
        author.setId(5L);
        author.setNickname("bob");
        author.setRole("USER");
        author.setBlacklisted(false);
        when(users.findById(5L)).thenReturn(Optional.of(author));
        when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContentReportHandleRequest request = new ContentReportHandleRequest();
        request.setAction("removeAndBlockAuthor");
        request.setNote("block reason");

        ContentReportResponse response = contentReportService.handle(8L, "admin", request);

        assertThat(response.getHandledBy()).isEqualTo("admin");
        assertThat(response.getStatus()).isNotBlank();
        assertThat(author.getBlacklisted()).isTrue();
        assertThat(author.getBlacklistReason()).isEqualTo("block reason");
        assertThat(post.getAuditStatus()).isNotBlank();
    }

    @Test
    void handleShouldRejectLegacyNicknameOnlyTargetAuthorWhenUserIdMissing() {
        ContentReport report = new ContentReport();
        report.setId(12L);
        report.setTargetType("post");
        report.setTargetId(20L);
        report.setReporter("alice");
        report.setReason("violation");
        report.setStatus("pending");
        report.setCreatedAt(LocalDateTime.now());
        when(reports.findById(12L)).thenReturn(Optional.of(report));

        MarketPost post = new MarketPost();
        post.setId(20L);
        post.setAuthor("bob");
        post.setAuthorUserId(null);
        when(posts.findById(20L)).thenReturn(Optional.of(post));

        ContentReportHandleRequest request = new ContentReportHandleRequest();
        request.setAction("removeAndBlockAuthor");
        request.setNote("block reason");

        assertThatThrownBy(() -> contentReportService.handle(12L, "admin", request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.USER_NOT_FOUND);
    }
}
