package io.github._3xhaust.root_server.domain.product.exception;

import io.github._3xhaust.root_server.global.common.exception.BaseException;

public class ProductException extends BaseException {
    public ProductException(ProductErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}

