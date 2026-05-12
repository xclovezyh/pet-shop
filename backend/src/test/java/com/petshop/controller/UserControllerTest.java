package com.petshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.api.GlobalExceptionHandler;
import com.petshop.dto.user.RegisterUserRequest;
import com.petshop.dto.user.UserResponse;
import com.petshop.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
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

    @MockBean
    private UserService userService;

    @Test
    void registerShouldReturnSuccessEnvelope() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("alice123");
        response.setNickname("alice");

        when(userService.register(any(RegisterUserRequest.class))).thenReturn(response);

        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("alice123");
        request.setPassword("secret123");
        request.setPhone("13800138000");
        request.setNickname("alice");
        request.setCode("123456");

        mockMvc.perform(post("/users/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.username").value("alice123"));
    }

    @Test
    void registerShouldReturnFailureEnvelopeWhenServiceThrows() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new ApiException(ApiErrorCode.USERNAME_ALREADY_EXISTS));

        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("alice123");
        request.setPassword("secret123");
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
}
