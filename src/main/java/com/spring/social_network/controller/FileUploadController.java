package com.spring.social_network.controller;

import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller xử lý việc tải lên file
 * 
 * Cung cấp các endpoint để:
 * - Tải lên file từ client lên server
 * - Hỗ trợ nhiều loại file khác nhau
 * - Trả về URL của file đã tải lên thành công
 * 
 * @author Spring Social Network Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Tải lên file từ client
     * 
     * @param file File cần tải lên (MultipartFile)
     * @return ResponseEntity chứa URL của file đã tải lên thành công
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileUploadService.uploadFile(file);
            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("File uploaded successfully")
                    .data(fileUrl)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Failed to upload file: " + e.getMessage())
                    .build());
        }
    }
}
