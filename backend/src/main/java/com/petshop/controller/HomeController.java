package com.petshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {
    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("name", "萌宠集市 API");
        data.put("status", "running");
        data.put("frontend", "http://127.0.0.1:5173/index.html");
        return data;
    }
}
