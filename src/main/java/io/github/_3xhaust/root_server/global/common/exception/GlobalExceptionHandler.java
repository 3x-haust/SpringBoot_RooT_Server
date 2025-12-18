package io.github._3xhaust.root_server.global.common.exception;

import io.github._3xhaust.root_server.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseException(BaseException ex) {
        return new ResponseEntity<>(ApiResponse.error(
                ex.getErrorCode().getHttpStatus(),
                ex.getErrorCode().getCode(),
                List.of(ex.getDetailMessage())
        ), ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(
                CommonErrorCode.INVALID_ARGUMENT.getHttpStatus(),
                CommonErrorCode.INVALID_ARGUMENT.getCode(),
                List.of(ex.getMessage())
        ), CommonErrorCode.INVALID_ARGUMENT.getHttpStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(
                CommonErrorCode.BAD_CREDENTIALS.getHttpStatus(),
                CommonErrorCode.BAD_CREDENTIALS.getCode(),
                List.of("이메일 또는 비밀번호가 올바르지 않습니다.")
        ), CommonErrorCode.BAD_CREDENTIALS.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        log.warn("Validation error: {}", messages);
        return new ResponseEntity<>(ApiResponse.error(
                CommonErrorCode.VALIDATION_ERROR.getHttpStatus(),
                CommonErrorCode.VALIDATION_ERROR.getCode(),
                messages
        ), CommonErrorCode.VALIDATION_ERROR.getHttpStatus());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFound(NoResourceFoundException ex) {
        log.info("Static resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(ApiResponse.error(
                CommonErrorCode.NOT_FOUND.getHttpStatus(),
                CommonErrorCode.NOT_FOUND.getCode(),
                List.of("요청하신 리소스를 찾을 수 없습니다.")
        ), CommonErrorCode.NOT_FOUND.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return new ResponseEntity<>(ApiResponse.error(
                CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(),
                CommonErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                List.of("서버 내부 오류가 발생했습니다.")
        ), CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
    }
}
