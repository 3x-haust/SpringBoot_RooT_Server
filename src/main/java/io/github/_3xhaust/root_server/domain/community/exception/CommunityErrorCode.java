package io.github._3xhaust.root_server.domain.community.exception;

import io.github._3xhaust.root_server.global.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommunityErrorCode implements ErrorCode {
    COMMUNITY_NOT_FOUND("COMMUNITY_NOT_FOUND", HttpStatus.NOT_FOUND, "커뮤니티를 찾을 수 없습니다."),
    COMMUNITY_POST_NOT_FOUND("COMMUNITY_POST_NOT_FOUND", HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    COMMUNITY_COMMENT_NOT_FOUND("COMMUNITY_COMMENT_NOT_FOUND", HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", HttpStatus.FORBIDDEN, "해당 리소스에 대한 권한이 없습니다."),
    INVALID_REACTION("INVALID_REACTION", HttpStatus.BAD_REQUEST, "잘못된 반응 값입니다. 1(좋아요) 또는 -1(싫어요)만 가능합니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
