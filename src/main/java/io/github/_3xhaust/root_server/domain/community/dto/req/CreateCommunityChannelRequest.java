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
public class CreateCommunityChannelRequest {
    @NotNull(message = "커뮤니티 ID는 필수입니다.")
    private Long communityId;

    @NotBlank(message = "게시판 이름은 필수입니다.")
    private String name;

    private String description;

    @NotBlank(message = "게시판 타입은 필수입니다.")
    private String type;
}
