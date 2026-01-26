package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.CommunityPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostListResponse {
    private Long id;
    private Long channelId;
    private String channelName;
    private String channelType;
    private String title;
    private String body;
    private String thumbnailUrl;
    private AuthorInfo author;
    private ReactionCount reactionCount;
    private Integer commentCount;
    private Instant createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String name;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionCount {
        private Long likeCount;
        private Long dislikeCount;
    }

    public static CommunityPostListResponse of(CommunityPost post) {
        long likeCount = post.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == 1)
                .count();
        long dislikeCount = post.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == -1)
                .count();

        String thumbnailUrl = post.getPostImages().isEmpty() ? null :
                post.getPostImages().get(0).getImage().getUrl();

        return CommunityPostListResponse.builder()
                .id(post.getId())
                .channelId(post.getChannel().getId())
                .channelName(post.getChannel().getName())
                .channelType(post.getChannel().getType())
                .title(post.getTitle())
                .body(post.getBody() != null && post.getBody().length() > 100 ?
                        post.getBody().substring(0, 100) + "..." : post.getBody())
                .thumbnailUrl(thumbnailUrl)
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .name(post.getAuthor().getName())
                        .profileImageUrl(post.getAuthor().getProfileImage() != null ?
                                post.getAuthor().getProfileImage().getUrl() : null)
                        .build())
                .reactionCount(ReactionCount.builder()
                        .likeCount(likeCount)
                        .dislikeCount(dislikeCount)
                        .build())
                .commentCount(post.getComments().size())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
