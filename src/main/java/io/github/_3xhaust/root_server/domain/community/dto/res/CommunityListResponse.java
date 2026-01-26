package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.Community;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityListResponse {
    private Long id;
    private String name;
    private String description;
    private Integer points;
    private Short gradeLevel;
    private Integer postCount;
    private Instant createdAt;

    public static CommunityListResponse of(Community community) {
        return CommunityListResponse.builder()
                .id(community.getId())
                .name(community.getName())
                .description(community.getDescription())
                .points(community.getPoints())
                .gradeLevel(community.getGradeLevel())
                .postCount(community.getChannels().stream()
                        .mapToInt(channel -> channel.getPosts().size())
                        .sum())
                .createdAt(community.getCreatedAt())
                .build();
    }
}
