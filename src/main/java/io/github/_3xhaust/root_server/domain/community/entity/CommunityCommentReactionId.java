package io.github._3xhaust.root_server.domain.community.entity;

import java.io.Serializable;
import java.util.Objects;

public class CommunityCommentReactionId implements Serializable {
    private Long user;
    private Long comment;

    public CommunityCommentReactionId() {}

    public CommunityCommentReactionId(Long user, Long comment) {
        this.user = user;
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunityCommentReactionId that = (CommunityCommentReactionId) o;
        return Objects.equals(user, that.user) && Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, comment);
    }
}
