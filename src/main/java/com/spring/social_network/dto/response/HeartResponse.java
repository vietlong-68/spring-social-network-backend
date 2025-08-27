package com.spring.social_network.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartResponse {
    private String id;
    private String userId;
    private String userFirstName;
    private String userLastName;
    private String userProfilePicture;
    private String postId;
    private String type;
    private String createdAt;
}
