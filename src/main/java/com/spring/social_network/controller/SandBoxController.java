package com.spring.social_network.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.spring.social_network.dto.ApiResponse;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.service.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Controller dành cho môi trường phát triển và kiểm thử
 * 
 * Cung cấp các endpoint để:
 * - Kiểm tra thông tin bảo mật và xác thực
 * - Test chức năng tải lên file
 * - Debug các vấn đề liên quan đến Spring Security
 * - Kiểm tra context và thread information
 * 
 * Lưu ý: Controller này chỉ nên sử dụng trong môi trường development
 * 
 * @author Spring Social Network Team
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/sandbox")
public class SandBoxController {

    private final FileUploadService fileUploadService;

    public SandBoxController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Lấy thông tin chi tiết về Spring Security Context
     * 
     * Endpoint này giúp debug các vấn đề liên quan đến bảo mật,
     * xác thực và phân quyền trong ứng dụng
     * 
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin chi tiết về security context
     */
    @GetMapping("/security-context")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecurityContextInfo(
            HttpServletRequest request) {
        Map<String, Object> securityInfo = new HashMap<>();
        var securityContext = SecurityContextHolder.getContext();
        securityInfo.put("contextClass", securityContext.getClass().getSimpleName());
        securityInfo.put("contextHashCode", securityContext.hashCode());
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put("name", authentication.getName());
            authInfo.put("principal", authentication.getPrincipal().toString());
            authInfo.put("principalClass", authentication.getPrincipal().getClass().getSimpleName());
            authInfo.put("authenticated", authentication.isAuthenticated());
            authInfo.put("credentials", authentication.getCredentials() != null ? "***" : null);
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                authInfo.put("authorities", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
                authInfo.put("authorityCount", authorities.size());
            } else {
                authInfo.put("authorities", "No authorities");
                authInfo.put("authorityCount", 0);
            }
            authInfo.put("details", authentication.getDetails());
            authInfo.put("detailsClass",
                    authentication.getDetails() != null ? authentication.getDetails().getClass().getSimpleName()
                            : null);
            securityInfo.put("authentication", authInfo);
        } else {
            securityInfo.put("authentication", "No authentication found");
        }
        Thread currentThread = Thread.currentThread();
        securityInfo.put("threadInfo", Map.of(
                "threadId", currentThread.getId(),
                "threadName", currentThread.getName(),
                "threadPriority", currentThread.getPriority()));
        securityInfo.put("timestamp", System.currentTimeMillis());
        securityInfo.put("timestampReadable", java.time.Instant.now().toString());
        return ResponseEntity
                .ok(ApiResponse.success(securityInfo, "Lấy thông tin security context thành công", request));
    }

    /**
     * Test chức năng tải lên file
     * 
     * Endpoint này giúp kiểm tra và debug chức năng tải lên file,
     * trả về thông tin chi tiết về file và quá trình upload
     * 
     * @param file    File cần test upload
     * @param request HttpServletRequest để lấy thông tin request
     * @return ResponseEntity chứa thông tin chi tiết về quá trình upload
     * @throws AppException Khi có lỗi xảy ra trong quá trình upload
     */
    @PostMapping("/upload-test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testFileUpload(
            @RequestParam(name = "file") MultipartFile file,
            HttpServletRequest request) {

        Map<String, Object> uploadInfo = new HashMap<>();

        try {
            uploadInfo.put("originalFilename", file.getOriginalFilename());
            uploadInfo.put("contentType", file.getContentType());
            uploadInfo.put("size", file.getSize());
            uploadInfo.put("isEmpty", file.isEmpty());

            String fileUrl = fileUploadService.uploadFile(file);
            uploadInfo.put("fileUrl", fileUrl);
            uploadInfo.put("uploadProvider", getCurrentUploadProvider());
            uploadInfo.put("uploadSuccess", true);

            return ResponseEntity.ok(ApiResponse.success(uploadInfo,
                    "Tải lên file thành công", request));

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }

    /**
     * Lấy tên của service upload file hiện tại
     * 
     * @return Tên class của service upload file
     */
    private String getCurrentUploadProvider() {
        return fileUploadService.getClass().getSimpleName();
    }
}
