package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.post.MarketPostRequest;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.model.AppUser;
import com.petshop.model.MarketPost;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketPostServiceTest {
    @Mock
    private MarketPostRepository posts;

    @Mock
    private AppUserRepository users;

    @InjectMocks
    private MarketPostService marketPostService;

    @Test
    void createShouldRejectNegativePrice() {
        MarketPostRequest request = new MarketPostRequest();
        request.setAuthor("alice");
        request.setCategory("猫咪");
        request.setTitle("test");
        request.setDescription("safe");
        request.setPrice(new BigDecimal("-1"));

        AppUser user = new AppUser();
        user.setId(1L);
        user.setNickname("alice");
        user.setRole("USER");
        user.setBlacklisted(false);

        assertThatThrownBy(() -> marketPostService.create(user, request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.POST_PRICE_INVALID);
    }

    @Test
    void createShouldFillDefaultStatusAndContact() {
        MarketPostRequest request = new MarketPostRequest();
        request.setAuthor("alice");
        request.setCategory("猫咪");
        request.setTitle("出一只猫");
        request.setDescription("仅站内沟通");
        request.setPrice(BigDecimal.ZERO);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setNickname("alice");
        user.setRole("USER");
        user.setBlacklisted(false);
        when(posts.save(any(MarketPost.class))).thenAnswer(invocation -> {
            MarketPost post = invocation.getArgument(0);
            post.setId(100L);
            return post;
        });

        MarketPostResponse response = marketPostService.create(user, request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo("在售");
        assertThat(response.getContact()).isEqualTo("站内私信");
        assertThat(response.getAuditStatus()).isEqualTo("审核通过");
    }

    @Test
    void listShouldHideRemovedAndPushClosedPostsToEnd() {
        MarketPost activeOld = post(1L, "在售旧帖", "在售", "审核通过", LocalDateTime.of(2026, 5, 1, 12, 0));
        MarketPost activeNew = post(2L, "在售新帖", "在售", "审核通过", LocalDateTime.of(2026, 5, 2, 12, 0));
        MarketPost reserved = post(3L, "已预约帖子", "已预约", "审核通过", LocalDateTime.of(2026, 5, 3, 12, 0));
        MarketPost closedNewest = post(4L, "已关闭帖子", "已关闭", "审核通过", LocalDateTime.of(2026, 5, 4, 12, 0));
        MarketPost removed = post(5L, "已下架帖子", "在售", "已下架", LocalDateTime.of(2026, 5, 5, 12, 0));

        when(posts.findAll()).thenReturn(Arrays.asList(closedNewest, removed, reserved, activeOld, activeNew));

        assertThat(marketPostService.list())
                .extracting(MarketPostResponse::getTitle)
                .containsExactly("在售新帖", "在售旧帖", "已预约帖子", "已关闭帖子");
    }

    @Test
    void detailShouldRejectRemovedPost() {
        MarketPost removed = post(5L, "已下架帖子", "在售", "已下架", LocalDateTime.of(2026, 5, 5, 12, 0));
        when(posts.findById(5L)).thenReturn(Optional.of(removed));

        assertThatThrownBy(() -> marketPostService.detail(5L))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.POST_NOT_FOUND);
    }

    @Test
    void updateShouldRejectSameNicknameWhenCurrentUserIdDoesNotOwnPost() {
        MarketPost existing = post(10L, "原帖", "在售", "审核通过", LocalDateTime.now());
        existing.setAuthor("alice");
        existing.setAuthorUserId(1L);

        AppUser currentUser = new AppUser();
        currentUser.setId(2L);
        currentUser.setNickname("alice");
        currentUser.setRole("USER");
        currentUser.setBlacklisted(false);

        MarketPostRequest request = new MarketPostRequest();
        request.setAuthor("alice");
        request.setCategory("猫咪");
        request.setTitle("修改标题");
        request.setDescription("safe");

        when(posts.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> marketPostService.update(10L, currentUser, request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.POST_AUTHOR_MISMATCH);
    }

    private MarketPost post(Long id, String title, String status, String auditStatus, LocalDateTime createdAt) {
        MarketPost post = new MarketPost();
        post.setId(id);
        post.setTitle(title);
        post.setStatus(status);
        post.setAuditStatus(auditStatus);
        post.setCreatedAt(createdAt);
        return post;
    }
}
