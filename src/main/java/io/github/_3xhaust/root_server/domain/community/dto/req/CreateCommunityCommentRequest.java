package io.github._3xhaust.root_server.domain.community.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommunityCommentRequest {
    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long postId;

    private Long parentId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String message;
}
