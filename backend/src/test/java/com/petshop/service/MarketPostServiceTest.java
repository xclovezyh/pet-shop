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
        user.setNickname("alice");
        user.setRole("USER");
        user.setBlacklisted(false);
        when(users.findByNickname("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> marketPostService.create(request))
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
        user.setNickname("alice");
        user.setRole("USER");
        user.setBlacklisted(false);
        when(users.findByNickname("alice")).thenReturn(Optional.of(user));
        when(posts.save(any(MarketPost.class))).thenAnswer(invocation -> {
            MarketPost post = invocation.getArgument(0);
            post.setId(100L);
            return post;
        });

        MarketPostResponse response = marketPostService.create(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isNotBlank();
        assertThat(response.getContact()).isNotBlank();
        assertThat(response.getAuditStatus()).isNotBlank();
    }
}
