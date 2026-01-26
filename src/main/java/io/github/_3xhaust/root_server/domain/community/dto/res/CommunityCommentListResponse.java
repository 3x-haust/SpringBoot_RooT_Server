package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.CommunityComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCommentListResponse {
    private Long id;
    private Long postId;
    private AuthorInfo author;
    private Long parentId;
    private String message;
    private ReactionCount reactionCount;
    private Integer replyCount;
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

    public static CommunityCommentListResponse of(CommunityComment comment) {
        long likeCount = comment.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == 1)
                .count();
        long dislikeCount = comment.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == -1)
                .count();

        return CommunityCommentListResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .author(AuthorInfo.builder()
                        .id(comment.getAuthor().getId())
                        .name(comment.getAuthor().getName())
                        .profileImageUrl(comment.getAuthor().getProfileImage() != null ?
                                comment.getAuthor().getProfileImage().getUrl() : null)
                        .build())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .message(comment.getMessage())
                .reactionCount(ReactionCount.builder()
                        .likeCount(likeCount)
                        .dislikeCount(dislikeCount)
                        .build())
                .replyCount(comment.getReplies() != null ? comment.getReplies().size() : 0)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
