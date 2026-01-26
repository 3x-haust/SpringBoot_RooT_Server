package io.github._3xhaust.root_server.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "Login request payload")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Schema(description = "User name", example = "username", required = true)
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "User password", example = "password123", required = true)
    @NotBlank(message = "Password is required")
    private String password;
}
