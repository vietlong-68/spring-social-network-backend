package com.spring.social_network.service;

import com.spring.social_network.dto.request.ForgotPasswordRequest;
import com.spring.social_network.dto.request.ResetPasswordRequest;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.model.PasswordResetToken;
import com.spring.social_network.model.User;
import com.spring.social_network.repository.PasswordResetTokenRepository;
import com.spring.social_network.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRY_HOURS = 1;

    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Email không tồn tại trong hệ thống"));

        tokenRepository.invalidateAllTokensByUserEmail(user.getEmail());

        String token = generateToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        String subject = "Đặt lại mật khẩu - " + user.getFirstName();
        String content = String.format("""
                Xin chào %s!

                Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản tại hệ thống.

                Mã đặt lại mật khẩu của bạn là: %s

                Mã này có hiệu lực trong %d giờ.

                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.

                Trân trọng,
                Đội ngũ hỗ trợ
                """, user.getFirstName(), token, TOKEN_EXPIRY_HOURS);

        emailService.sendEmail(user.getEmail(), subject, content);

        log.info("Token reset password đã được gửi đến email: {}", user.getEmail());
    }

    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(
                        () -> new AppException(ErrorCode.INVALID_TOKEN, "Token không hợp lệ hoặc đã được sử dụng"));

        if (resetToken.isExpired()) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED, "Token đã hết hạn");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        String subject = "Mật khẩu đã được đặt lại thành công";
        String content = String.format("""
                Xin chào %s!

                Mật khẩu của bạn đã được đặt lại thành công.

                Nếu bạn không thực hiện thao tác này, vui lòng liên hệ với chúng tôi ngay lập tức.

                Trân trọng,
                Đội ngũ hỗ trợ
                """, user.getFirstName());

        emailService.sendEmail(user.getEmail(), subject, content);

        log.info("Mật khẩu đã được reset thành công cho user: {}", user.getEmail());
    }

    private String generateToken() {

        return String.format("%06d", UUID.randomUUID().hashCode() % 1000000);
    }

    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteExpiredTokens(now);
        log.info("Đã dọn dẹp các token hết hạn");
    }
}