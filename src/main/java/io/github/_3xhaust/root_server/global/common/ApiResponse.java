package io.github._3xhaust.root_server.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Schema(description = "Standard API response wrapper")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "HTTP status code")
    private int statusCode;

    @Schema(description = "Response message(s)")
    private List<String> message;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error code", nullable = true)
    private String errorCode;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .message(List.of("요청이 성공적으로 처리되었습니다."))
                .data(data)
                .build();

    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .statusCode(HttpStatus.OK.value())
                .message(List.of(message))
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(HttpStatus status, T data) {
        return ApiResponse.<T>builder()
                .statusCode(status.value())
                .message(List.of("요청이 성공적으로 처리되었습니다."))
                .data(data)
                .build();

    }

    public static <T> ApiResponse<T> ok(HttpStatus status, T data, String message) {
        return ApiResponse.<T>builder()
                .statusCode(status.value())
                .message(List.of(message))
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String errorCode, List<String> messages) {
        return ApiResponse.<T>builder()
                .statusCode(httpStatus.value())
                .message(messages)
                .errorCode(errorCode)
                .build();
    }
}
