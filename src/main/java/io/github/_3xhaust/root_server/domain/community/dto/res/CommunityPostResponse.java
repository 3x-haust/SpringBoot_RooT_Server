package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.CommunityPost;
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
public class CommunityPostResponse {
    private Long id;
    private Long channelId;
    private String channelName;
    private String channelType;
    private AuthorInfo author;
    private String title;
    private String body;
    private List<String> imageUrls;
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
        private Short rating;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionCount {
        private Long likeCount;
        private Long dislikeCount;
    }

    public static CommunityPostResponse of(CommunityPost post) {
        long likeCount = post.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == 1)
                .count();
        long dislikeCount = post.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == -1)
                .count();

        return CommunityPostResponse.builder()
                .id(post.getId())
                .channelId(post.getChannel().getId())
                .channelName(post.getChannel().getName())
                .channelType(post.getChannel().getType())
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .name(post.getAuthor().getName())
                        .profileImageUrl(post.getAuthor().getProfileImage() != null ?
                                post.getAuthor().getProfileImage().getUrl() : null)
                        .rating(post.getAuthor().getRating())
                        .build())
                .title(post.getTitle())
                .body(post.getBody())
                .imageUrls(post.getPostImages().stream()
                        .map(pi -> pi.getImage().getUrl())
                        .toList())
                .reactionCount(ReactionCount.builder()
                        .likeCount(likeCount)
                        .dislikeCount(dislikeCount)
                        .build())
                .commentCount(post.getComments().size())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
