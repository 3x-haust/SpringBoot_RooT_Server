package io.github._3xhaust.root_server.domain.community.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommunityCommentRequest {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String message;
}
