package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.moment.MomentCommentRequest;
import com.petshop.dto.moment.MomentRequest;
import com.petshop.model.AppUser;
import com.petshop.model.Moment;
import com.petshop.model.MomentComment;
import com.petshop.repository.AppUserRepository;
import com.petshop.repository.MomentCommentRepository;
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
class MomentServiceTest {
    @Mock
    private MomentRepository moments;
    @Mock
    private MomentCommentRepository comments;
    @Mock
    private AppUserRepository users;

    @InjectMocks
    private MomentService momentService;

    @Test
    void updateShouldRejectSameNicknameWhenCurrentUserIdDoesNotOwnMoment() {
        Moment existing = moment(10L, "alice", 1L);
        AppUser currentUser = user(2L, "alice");
        MomentRequest request = request("猫咪日常");

        when(moments.findById(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> momentService.update(10L, currentUser, request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.MOMENT_AUTHOR_MISMATCH);
    }

    @Test
    void createCommentShouldPersistAuthorUserIdFromCurrentUser() {
        Moment existing = moment(10L, "alice", 1L);
        AppUser commenter = user(2L, "bob");
        MomentCommentRequest request = new MomentCommentRequest();
        request.setAuthor("ignored");
        request.setContent("可爱");

        when(moments.findById(10L)).thenReturn(Optional.of(existing));
        when(comments.save(any(MomentComment.class))).thenAnswer(invocation -> {
            MomentComment comment = invocation.getArgument(0);
            comment.setId(99L);
            return comment;
        });

        assertThat(momentService.createComment(10L, commenter, request).getId()).isEqualTo(99L);
    }

    @Test
    void updateShouldRejectLegacyNicknameOnlyMomentWhenUserIdsAreMissing() {
        Moment existing = moment(11L, "alice", null);
        AppUser currentUser = user(2L, "alice");
        MomentRequest request = request("鐚挭鏃ュ父");

        when(moments.findById(11L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> momentService.update(11L, currentUser, request))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.MOMENT_AUTHOR_MISMATCH);
    }

    private Moment moment(Long id, String author, Long authorUserId) {
        Moment moment = new Moment();
        moment.setId(id);
        moment.setAuthor(author);
        moment.setAuthorUserId(authorUserId);
        moment.setCategory("猫咪");
        moment.setContent("safe");
        moment.setCreatedAt(LocalDateTime.now());
        return moment;
    }

    private MomentRequest request(String content) {
        MomentRequest request = new MomentRequest();
        request.setAuthor("alice");
        request.setCategory("猫咪");
        request.setContent(content);
        return request;
    }

    private AppUser user(Long id, String nickname) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setNickname(nickname);
        user.setRole("USER");
        user.setBlacklisted(false);
        return user;
    }
}
