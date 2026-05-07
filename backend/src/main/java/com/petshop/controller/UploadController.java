package com.petshop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot);
        }

        Files.createDirectories(Paths.get(uploadDir));
        String fileName = UUID.randomUUID() + ext;
        Path target = Paths.get(uploadDir).resolve(fileName).toAbsolutePath().normalize();
        file.transferTo(target);
        Map<String, String> response = new HashMap<>();
        response.put("url", "/api/uploads/" + fileName);
        return response;
    }
}
