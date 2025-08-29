package com.spring.social_network.service;

import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileUploadService implements FileUploadService {

    @Value("${file.upload.local.path:./uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:5242880}")
    private long maxFileSize;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.getSize() > maxFileSize) {
            String errorMessage = String.format(
                    "File size exceeds the maximum limit. Current size: %.2f MB, Maximum allowed: %.2f MB",
                    file.getSize() / (1024.0 * 1024.0),
                    maxFileSize / (1024.0 * 1024.0));
            throw new AppException(ErrorCode.BAD_REQUEST, errorMessage);
        }

        try {

            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String contextPath = "";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                if (attributes != null && attributes.getRequest() != null) {
                    contextPath = attributes.getRequest().getContextPath();
                }
            } catch (Exception e) {

                contextPath = "/api";
            }

            return contextPath + "/uploads/" + filename;
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to upload file locally", e);
        }
    }
}
