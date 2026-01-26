package io.github._3xhaust.root_server.domain.community.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommunityPostRequest {
    private String title;
    private String body;
    private List<Long> imageIds;
}
