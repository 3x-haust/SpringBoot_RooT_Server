package io.github._3xhaust.root_server.domain.community.dto.res;

import io.github._3xhaust.root_server.domain.community.entity.CommunityComment;
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
public class CommunityCommentResponse {
    private Long id;
    private Long postId;
    private AuthorInfo author;
    private Long parentId;
    private String message;
    private ReactionCount reactionCount;
    private List<CommunityCommentResponse> replies;
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

    public static CommunityCommentResponse of(CommunityComment comment) {
        return of(comment, false);
    }

    public static CommunityCommentResponse of(CommunityComment comment, boolean includeReplies) {
        long likeCount = comment.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == 1)
                .count();
        long dislikeCount = comment.getReactions().stream()
                .filter(r -> r.getReaction() != null && r.getReaction() == -1)
                .count();

        List<CommunityCommentResponse> replies = null;
        if (includeReplies && comment.getReplies() != null) {
            replies = comment.getReplies().stream()
                    .map(CommunityCommentResponse::of)
                    .toList();
        }

        return CommunityCommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .author(AuthorInfo.builder()
                        .id(comment.getAuthor().getId())
                        .name(comment.getAuthor().getName())
                        .profileImageUrl(comment.getAuthor().getProfileImage() != null ?
                                comment.getAuthor().getProfileImage().getUrl() : null)
                        .rating(comment.getAuthor().getRating())
                        .build())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .message(comment.getMessage())
                .reactionCount(ReactionCount.builder()
                        .likeCount(likeCount)
                        .dislikeCount(dislikeCount)
                        .build())
                .replies(replies)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
