package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.CommunityChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityChannelResponse {
    private Long id;
    private Long communityId;
    private String communityName;
    private String name;
    private String description;
    private String type;
    private Integer postCount;
    private Instant createdAt;

    public static CommunityChannelResponse of(CommunityChannel channel) {
        return CommunityChannelResponse.builder()
                .id(channel.getId())
                .communityId(channel.getCommunity().getId())
                .communityName(channel.getCommunity().getName())
                .name(channel.getName())
                .description(channel.getDescription())
                .type(channel.getType())
                .postCount(channel.getPosts().size())
                .createdAt(channel.getCreatedAt())
                .build();
    }
}
