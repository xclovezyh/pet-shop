package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.message.MessageSendRequest;
import com.petshop.model.AppUser;
import com.petshop.model.PrivateMessageThread;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MarketPostRepository;
import com.petshop.repository.PrivateMessageRepository;
import com.petshop.repository.PrivateMessageThreadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivateMessageServiceTest {
    @Mock
    private PrivateMessageThreadRepository threads;
    @Mock
    private PrivateMessageRepository messages;
    @Mock
    private MarketPostRepository posts;
    @Mock
    private AppUserRepository users;

    @InjectMocks
    private PrivateMessageService messageService;

    @Test
    void sendShouldRejectSameNicknameWhenCurrentUserIdIsNotParticipant() {
        PrivateMessageThread thread = new PrivateMessageThread();
        thread.setId(5L);
        thread.setStarter("alice");
        thread.setStarterUserId(1L);
        thread.setRecipient("bob");
        thread.setRecipientUserId(3L);
        thread.setCreatedAt(LocalDateTime.now());
        thread.setUpdatedAt(LocalDateTime.now());

        AppUser currentUser = new AppUser();
        currentUser.setId(2L);
        currentUser.setNickname("alice");
        currentUser.setRole("USER");
        currentUser.setBlacklisted(false);

        MessageSendRequest request = new MessageSendRequest();
        request.setSender("ignored");
        request.setContent("hello");

        when(threads.findById(5L)).thenReturn(Optional.of(thread));

        assertThatThrownBy(() -> messageService.send(5L, currentUser, request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.MESSAGE_THREAD_FORBIDDEN);
    }
}

