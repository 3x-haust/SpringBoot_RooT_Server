package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.CommunityPostReaction;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPostReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityPostReactionRepository extends JpaRepository<CommunityPostReaction, CommunityPostReactionId> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<CommunityPostReaction> findByUserIdAndPostId(Long userId, Long postId);

    @Query("SELECT cpr FROM CommunityPostReaction cpr WHERE cpr.user.id = :userId AND cpr.post.id = :postId")
    Optional<CommunityPostReaction> findByUserIdAndPostIdWithEntities(@Param("userId") Long userId, @Param("postId") Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);
}
