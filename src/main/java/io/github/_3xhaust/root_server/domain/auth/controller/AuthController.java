package io.github._3xhaust.root_server.domain.auth.controller;

import io.github._3xhaust.root_server.domain.auth.dto.req.LoginRequest;
import io.github._3xhaust.root_server.domain.auth.dto.req.SignupRequest;
import io.github._3xhaust.root_server.domain.auth.dto.res.TokenResponse;
import io.github._3xhaust.root_server.domain.auth.service.AuthService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.github._3xhaust.root_server.domain.auth.dto.req.TokenRequest;

@Tag(name = "Authentication", description = "Authentication management APIs")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<TokenResponse> register(@Valid @RequestBody SignupRequest request) {
        TokenResponse response = authService.signup(request);
        return ApiResponse.ok(HttpStatus.CREATED, response);
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody TokenRequest request) {
        TokenResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ApiResponse.ok(response);
    }
}
