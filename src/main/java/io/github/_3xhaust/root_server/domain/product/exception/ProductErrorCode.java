package io.github._3xhaust.root_server.domain.product.exception;

import io.github._3xhaust.root_server.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", HttpStatus.FORBIDDEN, "해당 상품에 대한 권한이 없습니다."),
    INVALID_PRODUCT_TYPE("INVALID_PRODUCT_TYPE", HttpStatus.BAD_REQUEST, "잘못된 상품 타입입니다."),
    ALREADY_FAVORITED("ALREADY_FAVORITED", HttpStatus.CONFLICT, "이미 관심 상품으로 등록되어 있습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
