package io.github._3xhaust.root_server.domain.community.entity;

import io.github._3xhaust.root_server.domain.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityTag {
    @EmbeddedId
    private CommunityTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("communityId")
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Builder
    public CommunityTag(Community community, Tag tag) {
        this.id = new CommunityTagId(community.getId(), tag.getId());
        this.community = community;
        this.tag = tag;
    }
}
