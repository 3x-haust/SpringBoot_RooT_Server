package io.github._3xhaust.root_server.domain.community.entity;

import java.io.Serializable;
import java.util.Objects;

public class CommunityPostImageId implements Serializable {
    private Long post;
    private Long image;

    public CommunityPostImageId() {}

    public CommunityPostImageId(Long post, Long image) {
        this.post = post;
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommunityPostImageId that = (CommunityPostImageId) o;
        return Objects.equals(post, that.post) && Objects.equals(image, that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(post, image);
    }
}
