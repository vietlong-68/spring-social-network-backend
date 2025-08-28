package com.spring.social_network.config;

import com.spring.social_network.service.FileUploadService;
import com.spring.social_network.service.CloudinaryService;
import com.spring.social_network.service.LocalFileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FileUploadConfig {

    @Value("${file.upload.provider:cloudinary}")
    private String uploadProvider;

    @Bean
    @Primary
    public FileUploadService fileUploadService(CloudinaryService cloudinaryService,
            LocalFileUploadService localFileUploadService) {
        switch (uploadProvider.toLowerCase()) {
            case "local":
                return localFileUploadService;
            case "cloudinary":
            default:
                return cloudinaryService;
        }
    }
}
