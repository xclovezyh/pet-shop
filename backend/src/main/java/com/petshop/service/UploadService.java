package com.petshop.service;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;
import com.petshop.dto.upload.UploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadService {
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.upload-url-prefix:/api/uploads/}")
    private String uploadUrlPrefix;

    public UploadResponse upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ApiErrorCode.UPLOAD_FILE_REQUIRED);
        }

        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot).toLowerCase();
        }

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
        String fileName = UUID.randomUUID() + ext;
        Path target = uploadRoot.resolve(fileName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new ApiException(ApiErrorCode.UPLOAD_PATH_INVALID);
        }

        file.transferTo(target);
        return new UploadResponse(uploadUrlPrefix + fileName);
    }
}
