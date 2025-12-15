package io.github._3xhaust.root_server.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원가입 요청")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @Schema(description = "사용자 언어 코드", example = "en", required = true)
    @NotBlank(message = "언어 코드는 필수 값입니다.")
    @Size(max = 5, message = "언어 코드는 최대 5자리까지 가능합니다.")
    private String language;

    @Schema(description = "사용자 이름", example = "John Doe", required = true)
    @NotBlank(message = "이름은 필수 값입니다.")
    private String name;

    @Schema(description = "사용자 비밀번호", example = "password123", required = true)
    @NotBlank(message = "비밀번호는 필수 값입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @Schema(description = "사용자 이메일 주소", example = "user@example.com", required = true)
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 값입니다.")
    private String email;

    @Schema(description = "프로필 이미지 ID", example = "1", required = false)
    private Long profileImageId;
}
