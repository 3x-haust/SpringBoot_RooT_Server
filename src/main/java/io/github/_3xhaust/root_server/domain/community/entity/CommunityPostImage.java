package io.github._3xhaust.root_server.domain.community.entity;

import io.github._3xhaust.root_server.domain.image.entity.Image;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_post_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(CommunityPostImageId.class)
public class CommunityPostImage {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private CommunityPost post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @Builder
    public CommunityPostImage(CommunityPost post, Image image) {
        this.post = post;
        this.image = image;
    }
}
