package com.spring.social_network.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDto {

    private Long totalUsers;
    private Long activeUsers;
    private Long blockedUsers;
    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;
    private Long maleUsers;
    private Long femaleUsers;
    private Long usersWithProfilePicture;
    private Long usersWithPhone;
    private Long usersWithAddress;
}
