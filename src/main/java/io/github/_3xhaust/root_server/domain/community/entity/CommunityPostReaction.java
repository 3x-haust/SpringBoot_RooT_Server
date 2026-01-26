package io.github._3xhaust.root_server.domain.community.entity;

import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_post_reactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(CommunityPostReactionId.class)
public class CommunityPostReaction {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private CommunityPost post;

    @Column(nullable = false)
    private Short reaction;

    @Builder
    public CommunityPostReaction(User user, CommunityPost post, Short reaction) {
        this.user = user;
        this.post = post;
        this.reaction = reaction;
    }

    public void updateReaction(Short reaction) {
        this.reaction = reaction;
    }
}
