package io.github._3xhaust.root_server.domain.community.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommunityPostRequest {
    @NotNull(message = "채널 ID는 필수입니다.")
    private Long channelId;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String body;

    private List<Long> imageIds;
}
