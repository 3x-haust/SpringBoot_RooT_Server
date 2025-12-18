package io.github._3xhaust.root_server.domain.auth.service;

import io.github._3xhaust.root_server.domain.auth.dto.req.LoginRequest;
import io.github._3xhaust.root_server.domain.auth.dto.req.SignupRequest;
import io.github._3xhaust.root_server.domain.auth.dto.res.TokenResponse;
import io.github._3xhaust.root_server.domain.image.entity.Image;
import io.github._3xhaust.root_server.domain.image.repository.ImageRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ImageRepository imageRepository;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserException(UserErrorCode.EMAIL_DUPLICATED, "email=" + request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Image profileImage = null;
        if (request.getProfileImageId() != null) {
            profileImage = imageRepository.findById(request.getProfileImageId())
                    .orElseThrow(() -> new UserException(UserErrorCode.INVALID_USER_INPUT, "Profile image not found: id=" + request.getProfileImageId()));
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .language(request.getLanguage())
                .rating((short) 5)
                .profileImage(profileImage)
                .build();

        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(savedUser.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getEmail());

        Duration expiration = Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiry().toEpochMilli() - System.currentTimeMillis());
        refreshTokenService.saveRefreshToken(savedUser.getId(), refreshToken, expiration);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + request.getEmail()));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        Duration expiration = Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiry().toEpochMilli() - System.currentTimeMillis());
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken, expiration);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        Long userId = user.getId();

        if (!refreshTokenService.matches(userId, refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        Duration expiration = Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiry().toEpochMilli() - System.currentTimeMillis());
        refreshTokenService.saveRefreshToken(userId, newRefreshToken, expiration);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteRefreshToken(userId);
    }
}
