package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.trade.TradeIntentCreateRequest;
import com.petshop.dto.trade.TradeIntentResponse;
import com.petshop.service.TradeIntentService;
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
    public ApiResponse<List<TradeIntentResponse>> list(@RequestParam String user,
                                                       @RequestParam(defaultValue = "requester") String role) {
        return ApiResponse.success(tradeIntentService.list(user, role));
    }

    @PostMapping
    public ApiResponse<TradeIntentResponse> create(@RequestBody TradeIntentCreateRequest request) {
        return ApiResponse.success("交易意向已提交", tradeIntentService.create(request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<TradeIntentResponse> updateStatus(@PathVariable Long id,
                                                         @RequestParam String user,
                                                         @RequestParam String status) {
        return ApiResponse.success("交易意向状态已更新", tradeIntentService.updateStatus(id, user, status));
    }
}
