package io.github._3xhaust.root_server.domain.community.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommunityChannelRequest {
    private String name;
    private String description;
}
