package com.spring.social_network.config;

import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.spring.social_network.repository.InvalidatedTokenRepository;
import com.spring.social_network.repository.UserRepository;


@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final JwtConfig jwtConfig;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserRepository userRepository;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    public CustomJwtDecoder(JwtConfig jwtConfig, InvalidatedTokenRepository invalidatedTokenRepository,
            UserRepository userRepository) {
        this.jwtConfig = jwtConfig;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            String jti = claimsSet.getJWTID();

            if (invalidatedTokenRepository.existsById(jti)) {
                throw new JwtException("Token đã được đăng xuất");
            }

            String userEmail = claimsSet.getSubject();
            if (userEmail != null) {
                userRepository.findByEmail(userEmail).ifPresent(user -> {
                    if (user.getIsBlocked() != null && user.getIsBlocked()) {
                        throw new RuntimeException("Tài khoản đã bị khóa. Lý do: " +
                                (user.getBlockReason() != null ? user.getBlockReason() : "Không có thông tin"));
                    }
                });
            }

        } catch (RuntimeException e) {

            throw e;
        } catch (Exception e) {
            throw new JwtException("Định dạng token không hợp lệ: " + e.getMessage());
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(jwtConfig.getSignerKey().getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}