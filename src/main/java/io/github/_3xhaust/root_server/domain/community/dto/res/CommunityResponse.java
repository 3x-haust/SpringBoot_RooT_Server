package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.Community;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityResponse {
    private Long id;
    private OwnerInfo owner;
    private String name;
    private String description;
    private Integer points;
    private Short gradeLevel;
    private List<String> tags;
    private Instant createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long id;
        private String name;
        private String profileImageUrl;
        private Short rating;
    }

    public static CommunityResponse of(Community community) {
        return CommunityResponse.builder()
                .id(community.getId())
                .owner(OwnerInfo.builder()
                        .id(community.getOwner().getId())
                        .name(community.getOwner().getName())
                        .profileImageUrl(community.getOwner().getProfileImage() != null ?
                                community.getOwner().getProfileImage().getUrl() : null)
                        .rating(community.getOwner().getRating())
                        .build())
                .name(community.getName())
                .description(community.getDescription())
                .points(community.getPoints())
                .gradeLevel(community.getGradeLevel())
                .tags(null)
                .createdAt(community.getCreatedAt())
                .build();
    }
}
