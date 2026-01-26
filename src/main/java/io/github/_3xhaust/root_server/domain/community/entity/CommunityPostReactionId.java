package io.github._3xhaust.root_server.domain.community.entity;

import java.io.Serializable;
import java.util.Objects;

public class CommunityPostReactionId implements Serializable {
    private Long user;
    private Long post;

    public CommunityPostReactionId() {}

    public CommunityPostReactionId(Long user, Long post) {
        this.user = user;
        this.post = post;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunityPostReactionId that = (CommunityPostReactionId) o;
        return Objects.equals(user, that.user) && Objects.equals(post, that.post);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, post);
    }
}
