package com.petshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.api.GlobalExceptionHandler;
import com.petshop.dto.user.AuthSessionResponse;
import com.petshop.dto.user.RegisterUserRequest;
import com.petshop.dto.user.UserResponse;
import com.petshop.model.AppUser;
import com.petshop.service.AdminSessionService;
import com.petshop.service.UserJwtService;
import com.petshop.service.UserService;
import com.petshop.service.UserSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserController userController;

    @MockBean
    private UserService userService;

    @MockBean
    private UserSessionService userSessionService;

    @MockBean
    private UserJwtService userJwtService;

    @MockBean
    private AdminSessionService adminSessionService;

    @Test
    void registerShouldReturnSuccessEnvelope() throws Exception {
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setUsername("alice123");
        user.setNickname("alice");

        AuthSessionResponse response = new AuthSessionResponse();
        response.setToken("token-1");
        response.setUser(user);

        when(userService.register(any(RegisterUserRequest.class))).thenReturn(response);

        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("alice123");
        request.setPassword("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        request.setPhone("13800138000");
        request.setNickname("alice");
        request.setCode("123456");

        mockMvc.perform(post("/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.token").value("token-1"))
                .andExpect(jsonPath("$.data.user.username").value("alice123"));
    }

    @Test
    void registerShouldReturnFailureEnvelopeWhenServiceThrows() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new ApiException(ApiErrorCode.USERNAME_ALREADY_EXISTS));

        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("alice123");
        request.setPassword("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        request.setPhone("13800138000");
        request.setNickname("alice");
        request.setCode("123456");

        mockMvc.perform(post("/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("USER_400_001"));
    }

    @Test
    void legacyBlacklistEndpointShouldRejectAdminAction() {
        assertThatThrownBy(() -> userController.blacklist(9L, superAdminUser(), "旧入口限制"))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.FORBIDDEN);

        verifyNoInteractions(userService);
    }

    @Test
    void legacyRoleEndpointShouldRejectAdminAction() {
        assertThatThrownBy(() -> userController.updateRole(9L, superAdminUser(), "SUPER_ADMIN"))
                .isInstanceOf(ApiException.class)
                .extracting(error -> ((ApiException) error).getErrorCode())
                .isEqualTo(ApiErrorCode.FORBIDDEN);

        verifyNoInteractions(userService);
    }

    private AppUser superAdminUser() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setNickname("legacy-root");
        user.setRole("SUPER_ADMIN");
        user.setBlacklisted(false);
        return user;
    }
}
