package com.spring.social_network.service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.spring.social_network.config.JwtConfig;
import com.spring.social_network.dto.request.IntrospectRequest;
import com.spring.social_network.dto.request.LoginRequest;
import com.spring.social_network.dto.request.LogoutRequest;
import com.spring.social_network.dto.request.RefreshTokenRequest;
import com.spring.social_network.dto.request.RegisterRequest;
import com.spring.social_network.dto.response.IntrospectResponse;
import com.spring.social_network.dto.response.LoginResponse;
import com.spring.social_network.dto.response.RefreshTokenResponse;
import com.spring.social_network.dto.response.RegisterResponse;
import com.spring.social_network.exception.AppException;
import com.spring.social_network.exception.ErrorCode;
import com.spring.social_network.model.InvalidatedToken;
import com.spring.social_network.model.User;
import com.spring.social_network.model.Role;
import com.spring.social_network.model.RoleType;
import com.spring.social_network.repository.UserRepository;
import com.spring.social_network.repository.InvalidatedTokenRepository;
import com.spring.social_network.repository.RoleRepository;
import com.spring.social_network.mapper.UserMapper;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtConfig jwtConfig,
            InvalidatedTokenRepository invalidatedTokenRepository, RoleRepository roleRepository,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
        this.invalidatedTokenRepository = invalidatedTokenRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    public RegisterResponse handleRegister(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS,
                    "Email đã tồn tại: " + registerRequest.getEmail());
        }

        User user = userMapper.toEntityFromRegisterRequest(registerRequest);

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        Role userRole = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_SERVER_ERROR,
                        "Không tìm thấy role USER mặc định"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .success(true)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFirstName() + " " + savedUser.getLastName())
                .message("Đăng ký tài khoản thành công")
                .build();
    }

    public LoginResponse handleLogin(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getIsBlocked() != null && user.getIsBlocked()) {
            throw new AppException(ErrorCode.USER_IS_BLOCKED,
                    "Tài khoản của bạn đã bị khóa. Lý do: " +
                            (user.getBlockReason() != null ? user.getBlockReason() : "Không có thông tin"));
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        String token = generateToken(user);
        return LoginResponse.builder().success(true).token(token).build();
    }

    public void handleLogout(LogoutRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        boolean isValid = verifyToken(token);
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        String jti = claimsSet.getJWTID();
        Date expirationTime = claimsSet.getExpirationTime();

        invalidatedTokenRepository.save(InvalidatedToken.builder().id(jti).expiresAt(expirationTime).build());
    }

    public RefreshTokenResponse handleRefreshToken(RefreshTokenRequest refreshTokenRequest)
            throws JOSEException, ParseException {
        String token = refreshTokenRequest.getToken();

        boolean isValid = verifyToken(token);
        if (!isValid) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Invalid refresh token");
        }

        SignedJWT signedJWT = SignedJWT.parse(token);
        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        String userEmail = claimsSet.getSubject();
        String userId = (String) claimsSet.getClaim("userId");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "Không tìm thấy người dùng"));

        if (user.getIsBlocked() != null && user.getIsBlocked()) {
            throw new AppException(ErrorCode.USER_IS_BLOCKED,
                    "Tài khoản của bạn đã bị khóa. Lý do: " +
                            (user.getBlockReason() != null ? user.getBlockReason() : "Không có thông tin"));
        }

        String jti = claimsSet.getJWTID();
        Date expirationTime = claimsSet.getExpirationTime();

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(jti)
                .expiresAt(expirationTime)
                .build());

        String newToken = generateToken(user);

        return RefreshTokenResponse.builder()
                .success(true)
                .token(newToken)
                .message("Token làm mới thành công")
                .build();
    }

    private List<String> buildScopes(Set<String> roleNames) {
        return roleNames.stream()
                .map(role -> role)
                .collect(Collectors.toList());
    }

    private String generateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().getCode())
                .collect(Collectors.toSet());

        JWTClaimsSet jwsClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("social-network.com")
                .audience("social-network.com")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * jwtConfig.getExpirationHours()))
                .jwtID(UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("scope", buildScopes(roleNames))
                .build();

        JWSObject jwsObject = new JWSObject(jwsHeader, new Payload(jwsClaimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(jwtConfig.getSignerKey().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return jwsObject.serialize();
    }

    public IntrospectResponse handleIntrospect(IntrospectRequest introspectRequest)
            throws JOSEException, ParseException {
        String token = introspectRequest.getToken();

        boolean isValid = verifyToken(token);

        return IntrospectResponse.builder().valid(isValid).build();
    }

    private boolean verifyToken(String token) throws JOSEException, ParseException {
        try {
            JWSVerifier verifier = new MACVerifier(jwtConfig.getSignerKey().getBytes(StandardCharsets.UTF_8));
            SignedJWT signedJWT = SignedJWT.parse(token);
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            if (expirationTime.before(new Date())) {
                return false;
            }

            boolean isValid = signedJWT.verify(verifier);
            if (!isValid) {
                return false;
            }

            if (!signedJWT.getJWTClaimsSet().getAudience().contains("social-network.com")) {
                return false;
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            String jti = claimsSet.getJWTID();

            if (invalidatedTokenRepository.existsById(jti)) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
