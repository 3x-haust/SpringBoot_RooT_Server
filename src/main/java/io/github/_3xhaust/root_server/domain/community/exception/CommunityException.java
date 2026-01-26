package io.github._3xhaust.root_server.domain.community.exception;

import io.github._3xhaust.root_server.global.common.exception.BaseException;

public class CommunityException extends BaseException {
    public CommunityException(CommunityErrorCode errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }
}
