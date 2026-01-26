package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    Page<CommunityPost> findByChannelId(Long channelId, Pageable pageable);

    Page<CommunityPost> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT cp FROM CommunityPost cp WHERE cp.channel.id = :channelId")
    Page<CommunityPost> findByChannelIdWithChannel(@Param("channelId") Long channelId, Pageable pageable);
}
