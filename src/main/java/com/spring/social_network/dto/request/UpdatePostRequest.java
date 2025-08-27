package com.spring.social_network.dto.request;

import com.spring.social_network.model.post.PostPrivacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {
    private String content;
    private List<MultipartFile> files;
    private PostPrivacy privacy;
    private List<String> hashtags;
}
