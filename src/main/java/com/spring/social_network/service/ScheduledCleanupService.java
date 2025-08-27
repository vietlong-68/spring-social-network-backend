package com.spring.social_network.service;

import com.spring.social_network.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            log.info("Bắt đầu dọn dẹp token đã quá hạn...");

            Date currentTime = new Date();

            int deletedCount = invalidatedTokenRepository.deleteExpiredTokens(currentTime);

            log.info("Hoàn thành dọn dẹp! Đã xóa {} token quá hạn", deletedCount);

        } catch (Exception e) {
            log.error("Lỗi khi dọn dẹp token quá hạn: {}", e.getMessage(), e);
        }
    }
}
