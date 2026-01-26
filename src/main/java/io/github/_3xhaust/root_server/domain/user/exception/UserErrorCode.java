package io.github._3xhaust.root_server.domain.user.exception;

import io.github._3xhaust.root_server.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATED("EMAIL_DUPLICATED", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NAME_DUPLICATED("NAME_DUPLICATED", HttpStatus.CONFLICT, "이미 사용 중인 이름입니다."),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    INVALID_USER_INPUT("INVALID_USER_INPUT", HttpStatus.BAD_REQUEST, "잘못된 사용자 요청입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
