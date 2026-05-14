package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.model.AppUser;
import com.petshop.model.TradeIntent;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.TradeIntentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeIntentServiceTest {
    @Mock
    private TradeIntentRepository intents;
    @Mock
    private MarketPostRepository posts;
    @Mock
    private AppUserRepository users;

    @InjectMocks
    private TradeIntentService tradeIntentService;

    @Test
    void updateStatusShouldRejectSameNicknameWhenCurrentUserIdIsNotParticipant() {
        TradeIntent intent = new TradeIntent();
        intent.setId(7L);
        intent.setRequester("alice");
        intent.setRequesterUserId(1L);
        intent.setOwner("bob");
        intent.setOwnerUserId(3L);
        intent.setStatus("pending");
        intent.setCreatedAt(LocalDateTime.now());
        intent.setUpdatedAt(LocalDateTime.now());

        AppUser currentUser = new AppUser();
        currentUser.setId(2L);
        currentUser.setNickname("alice");
        currentUser.setRole("USER");
        currentUser.setBlacklisted(false);

        when(intents.findById(7L)).thenReturn(Optional.of(intent));

        assertThatThrownBy(() -> tradeIntentService.updateStatus(7L, currentUser, "cancel"))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.TRADE_INTENT_FORBIDDEN);
    }

    @Test
    void listShouldIgnoreLegacyNicknameOnlyIntentsWhenUserIdsAreMissing() {
        TradeIntent intent = new TradeIntent();
        intent.setId(8L);
        intent.setRequester("alice");
        intent.setOwner("bob");
        intent.setStatus("pending");
        intent.setCreatedAt(LocalDateTime.now());
        intent.setUpdatedAt(LocalDateTime.now());

        AppUser currentUser = new AppUser();
        currentUser.setId(2L);
        currentUser.setNickname("alice");
        currentUser.setRole("USER");
        currentUser.setBlacklisted(false);

        when(intents.findByRequesterUserIdOrderByUpdatedAtDesc(2L)).thenReturn(Collections.emptyList());

        assertThat(tradeIntentService.list(currentUser, "requester")).isEmpty();
    }
}
