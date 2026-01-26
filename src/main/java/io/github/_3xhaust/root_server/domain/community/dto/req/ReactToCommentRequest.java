package io.github._3xhaust.root_server.domain.community.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactToCommentRequest {
    @NotNull(message = "반응 값은 필수입니다.")
    private Short reaction;
}
