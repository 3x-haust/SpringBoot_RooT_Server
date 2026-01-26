package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.CommunityCommentReaction;
import io.github._3xhaust.root_server.domain.community.entity.CommunityCommentReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommunityCommentReactionRepository extends JpaRepository<CommunityCommentReaction, CommunityCommentReactionId> {
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    Optional<CommunityCommentReaction> findByUserIdAndCommentId(Long userId, Long commentId);

    @Query("SELECT ccr FROM CommunityCommentReaction ccr WHERE ccr.user.id = :userId AND ccr.comment.id = :commentId")
    Optional<CommunityCommentReaction> findByUserIdAndCommentIdWithEntities(@Param("userId") Long userId, @Param("commentId") Long commentId);

    void deleteByUserIdAndCommentId(Long userId, Long commentId);
}
