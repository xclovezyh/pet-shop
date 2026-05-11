package com.petshop.controller;

import com.petshop.api.ApiResponse;
import com.petshop.dto.upload.UploadResponse;
import com.petshop.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping
    public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.success("图片上传成功", uploadService.upload(file));
    }
}
