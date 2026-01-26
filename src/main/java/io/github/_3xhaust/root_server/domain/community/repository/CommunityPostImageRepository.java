package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.CommunityPostImage;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPostImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostImageRepository extends JpaRepository<CommunityPostImage, CommunityPostImageId> {
    List<CommunityPostImage> findByPostId(Long postId);

    void deleteByPostId(Long postId);
}
