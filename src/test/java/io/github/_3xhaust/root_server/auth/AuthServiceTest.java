package io.github._3xhaust.root_server.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github._3xhaust.root_server.domain.auth.dto.req.LoginRequest;
import io.github._3xhaust.root_server.domain.auth.dto.req.SignupRequest;
import io.github._3xhaust.root_server.domain.auth.dto.req.TokenRequest;
import io.github._3xhaust.root_server.domain.image.dto.ImageUploadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthServiceTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공 테스트 (프로필 이미지 없음)")
    void signup_success_without_profile_image() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .name("MockUser")
                .language("ko")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("회원가입 성공 테스트 (프로필 이미지 있음)")
    void signup_success_with_profile_image() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test_image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                Files.readAllBytes(
                        Paths.get("src/test/resources/image/test_image.jpg")
                )
        );

        MvcResult uploadResult = mockMvc.perform(
                        multipart("/api/v1/images/upload")
                                .file(file)
                )
                .andExpect(status().isCreated())
                .andReturn();

        String uploadResponse = uploadResult.getResponse().getContentAsString();

        ImageUploadResponse imageResponse =
                objectMapper.readValue(uploadResponse, ImageUploadResponse.class);

        Long imageId = imageResponse.getId();
        SignupRequest signupRequest = SignupRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .name("MockUser")
                .language("ko")
                .profileImageId(imageId)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }


    @Test
    @DisplayName("로그인 성공 테스트")
    void login_success() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .name("MockUser")
                .language("ko")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

    }

    @Test
    @DisplayName("회원가입 실패 테스트 (중복된 이메일)")
    void signup_fail_duplicate_email() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .name("MockUser")
                .language("ko")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        SignupRequest signupRequest2 = SignupRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .name("MockUser")
                .language("ko")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest2)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("회원가입 실패 테스트 (프로필 이미지 존재하지 않음)")
    void signup_fail_when_profile_image_does_not_exist() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .name("MockUser")
                .language("ko")
                .profileImageId(1L)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 성공 테스트")
    void refresh_token_success() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .name("MockUser")
                .language("ko")
                .build();

        MvcResult signupResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();

        String responseBody = signupResult.getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(responseBody);
        String refreshToken = root.path("data").path("refreshToken").asText();

        TokenRequest tokenRequest = TokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 실패 테스트 (유효하지 않은 토큰)")
    void refresh_token_fail_invalid_token() throws Exception {
        TokenRequest tokenRequest = TokenRequest.builder()
                .refreshToken("invalid_token")
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 실패 테스트 (토큰은 유효하지만 사용자 없음)")
    void refresh_token_fail_user_not_found() throws Exception {
        SignupRequest signupRequest = SignupRequest.builder()
                .email("mockuser@example.com")
                .password("mockpassword123")
                .name("MockUser")
                .language("ko")
                .build();

        MvcResult signupResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = signupResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);
        String refreshToken = root.path("data").path("refreshToken").asText();
        String accessToken = root.path("data").path("accessToken").asText();

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        TokenRequest tokenRequest = TokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isNotFound())
                .andReturn();
    }
}
