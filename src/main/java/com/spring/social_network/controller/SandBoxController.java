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

@RestController
@RequestMapping("/sandbox")
public class SandBoxController {

    private final FileUploadService fileUploadService;

    public SandBoxController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

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

    private String getCurrentUploadProvider() {
        return fileUploadService.getClass().getSimpleName();
    }
}
