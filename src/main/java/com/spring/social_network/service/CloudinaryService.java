package com.spring.social_network.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService implements FileUploadService {

    private final Cloudinary cloudinary;

    @Value("${file.upload.max-size:1048576}")
    private long maxFileSize;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.getSize() > maxFileSize) {
            String errorMessage = String.format(
                    "Kích thước file vượt quá giới hạn tối đa. Kích thước hiện tại: %.2f MB, Giới hạn cho phép: %.2f MB",
                    file.getSize() / (1024.0 * 1024.0),
                    maxFileSize / (1024.0 * 1024.0));
            throw new AppException(ErrorCode.BAD_REQUEST, errorMessage);
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return (String) uploadResult.get("url");
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Tải lên file lên Cloudinary thất bại", e);
        }
    }
}