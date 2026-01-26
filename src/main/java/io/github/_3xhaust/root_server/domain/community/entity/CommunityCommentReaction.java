package io.github._3xhaust.root_server.domain.community.entity;

import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_comment_reactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(CommunityCommentReactionId.class)
public class CommunityCommentReaction {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private CommunityComment comment;

    @Column(nullable = false)
    private Short reaction;

    @Builder
    public CommunityCommentReaction(User user, CommunityComment comment, Short reaction) {
        this.user = user;
        this.comment = comment;
        this.reaction = reaction;
    }

    public void updateReaction(Short reaction) {
        this.reaction = reaction;
    }
}
