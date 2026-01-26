package io.github._3xhaust.root_server.domain.community.repository;

import io.github._3xhaust.root_server.domain.community.entity.CommunityComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    Page<CommunityComment> findByPostId(Long postId, Pageable pageable);

    List<CommunityComment> findByPostIdAndParentIdIsNull(Long postId);

    List<CommunityComment> findByParentId(Long parentId);

    Page<CommunityComment> findByAuthorId(Long authorId, Pageable pageable);

    @Query("SELECT cc FROM CommunityComment cc WHERE cc.post.id = :postId AND cc.parent IS NULL ORDER BY cc.createdAt ASC")
    List<CommunityComment> findTopLevelCommentsByPostId(@Param("postId") Long postId);
}
