package io.github._3xhaust.root_server.domain.community.service;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityCommentRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.ReactToCommentRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdateCommunityCommentRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityCommentListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityCommentResponse;
import io.github._3xhaust.root_server.domain.community.entity.CommunityComment;
import io.github._3xhaust.root_server.domain.community.entity.CommunityCommentReaction;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPost;
import io.github._3xhaust.root_server.domain.community.exception.CommunityErrorCode;
import io.github._3xhaust.root_server.domain.community.exception.CommunityException;
import io.github._3xhaust.root_server.domain.community.repository.CommunityCommentReactionRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityCommentRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityPostRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityCommentService {

    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityCommentReactionRepository communityCommentReactionRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    public Page<CommunityCommentListResponse> getComments(Long postId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "createdAt"));
        return communityCommentRepository.findByPostId(postId, pageable)
                .map(CommunityCommentListResponse::of);
    }

    public List<CommunityCommentResponse> getCommentsWithReplies(Long postId) {
        List<CommunityComment> topLevelComments = communityCommentRepository.findTopLevelCommentsByPostId(postId);
        return topLevelComments.stream()
                .map(comment -> {
                    CommunityCommentResponse response = CommunityCommentResponse.of(comment);
                    List<CommunityComment> replies = communityCommentRepository.findByParentId(comment.getId());
                    List<CommunityCommentResponse> replyResponses = replies.stream()
                            .map(CommunityCommentResponse::of)
                            .toList();
                    return CommunityCommentResponse.builder()
                            .id(response.getId())
                            .postId(response.getPostId())
                            .author(response.getAuthor())
                            .parentId(response.getParentId())
                            .message(response.getMessage())
                            .reactionCount(response.getReactionCount())
                            .replies(replyResponses)
                            .createdAt(response.getCreatedAt())
                            .build();
                })
                .toList();
    }

    public CommunityCommentResponse getCommentById(Long id) {
        CommunityComment comment = communityCommentRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "id=" + id));
        return CommunityCommentResponse.of(comment);
    }

    @Transactional
    public CommunityCommentResponse createComment(String name, CreateCommunityCommentRequest request) {
        User author = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityPost post = communityPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND, "id=" + request.getPostId()));

        CommunityComment parent = null;
        if (request.getParentId() != null) {
            parent = communityCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "id=" + request.getParentId()));
        }

        CommunityComment comment = CommunityComment.builder()
                .post(post)
                .author(author)
                .parent(parent)
                .message(request.getMessage())
                .build();

        CommunityComment savedComment = communityCommentRepository.save(comment);

        if (parent != null) {
            parent.addReply(savedComment);
        }

        return CommunityCommentResponse.of(savedComment);
    }

    @Transactional
    public CommunityCommentResponse updateComment(String name, Long id, UpdateCommunityCommentRequest request) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityComment comment = communityCommentRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "id=" + id));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "commentId=" + id);
        }

        comment.update(request.getMessage());
        return CommunityCommentResponse.of(comment);
    }

    @Transactional
    public void deleteComment(String name, Long id) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityComment comment = communityCommentRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "id=" + id));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "commentId=" + id);
        }

        communityCommentRepository.delete(comment);
    }

    @Transactional
    public void reactToComment(String name, Long commentId, ReactToCommentRequest request) {
        if (request.getReaction() != 1 && request.getReaction() != -1) {
            throw new CommunityException(CommunityErrorCode.INVALID_REACTION, "reaction=" + request.getReaction());
        }

        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityComment comment = communityCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_COMMENT_NOT_FOUND, "id=" + commentId));

        Optional<CommunityCommentReaction> existingReaction = communityCommentReactionRepository
                .findByUserIdAndCommentIdWithEntities(user.getId(), commentId);

        if (existingReaction.isPresent()) {
            CommunityCommentReaction reaction = existingReaction.get();
            if (reaction.getReaction().equals(request.getReaction())) {
                communityCommentReactionRepository.delete(reaction);
            } else {
                reaction.updateReaction(request.getReaction());
            }
        } else {
            CommunityCommentReaction reaction = CommunityCommentReaction.builder()
                    .user(user)
                    .comment(comment)
                    .reaction(request.getReaction())
                    .build();
            communityCommentReactionRepository.save(reaction);
        }
    }
}
