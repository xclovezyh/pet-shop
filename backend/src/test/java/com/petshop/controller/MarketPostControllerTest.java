package com.petshop.controller;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.api.GlobalExceptionHandler;
import com.petshop.dto.post.MarketPostRequest;
import com.petshop.dto.post.MarketPostResponse;
import com.petshop.service.MarketPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MarketPostController.class)
@Import(GlobalExceptionHandler.class)
class MarketPostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarketPostService marketPostService;

    @Test
    void createShouldWrapResponseInApiEnvelope() throws Exception {
        MarketPostResponse response = new MarketPostResponse();
        response.setId(9L);
        response.setTitle("出猫");
        response.setCategory("猫咪");
        response.setPrice(BigDecimal.ZERO);

        when(marketPostService.create(any(MarketPostRequest.class))).thenReturn(response);

        String payload = "{\"author\":\"alice\",\"title\":\"出猫\",\"category\":\"猫咪\",\"description\":\"仅站内沟通\",\"price\":0}";

        mockMvc.perform(post("/posts")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(9L))
                .andExpect(jsonPath("$.data.title").value("出猫"));
    }

    @Test
    void createShouldReturnErrorCodeWhenValidationFails() throws Exception {
        when(marketPostService.create(any(MarketPostRequest.class)))
                .thenThrow(new ApiException(ApiErrorCode.POST_CATEGORY_REQUIRED));

        String payload = "{\"author\":\"alice\",\"title\":\"出猫\",\"description\":\"仅站内沟通\",\"price\":0}";

        mockMvc.perform(post("/posts")
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_400_001"));
    }
}
