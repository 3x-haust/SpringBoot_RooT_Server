package io.github._3xhaust.root_server.domain.community.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommunityRequest {
    @NotBlank(message = "커뮤니티 이름은 필수입니다.")
    private String name;

    private String description;

    private List<String> tags;
}
