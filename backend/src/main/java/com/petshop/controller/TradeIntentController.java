package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.trade.TradeIntentCreateRequest;
import com.petshop.dto.trade.TradeIntentResponse;
import com.petshop.model.AppUser;
import com.petshop.service.TradeIntentService;
import com.petshop.support.CurrentUser;
import com.petshop.support.UserGuard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/trade-intents")
public class TradeIntentController {
    private final TradeIntentService tradeIntentService;

    public TradeIntentController(TradeIntentService tradeIntentService) {
        this.tradeIntentService = tradeIntentService;
    }

    @GetMapping
    public ApiResponse<List<TradeIntentResponse>> list(@CurrentUser AppUser currentUser,
                                                       @RequestParam(defaultValue = "requester") String role) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "查看交易意向");
        return ApiResponse.success(tradeIntentService.list(user, role));
    }

    @PostMapping
    public ApiResponse<TradeIntentResponse> create(@CurrentUser AppUser currentUser,
                                                   @RequestBody TradeIntentCreateRequest request) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "提交交易意向");
        return ApiResponse.success("交易意向已提交", tradeIntentService.create(user, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<TradeIntentResponse> updateStatus(@PathVariable Long id,
                                                         @CurrentUser AppUser currentUser,
                                                         @RequestParam String status) {
        AppUser user = UserGuard.requireAuthenticated(currentUser, "处理交易意向");
        return ApiResponse.success("交易意向状态已更新", tradeIntentService.updateStatus(id, user, status));
    }
}
