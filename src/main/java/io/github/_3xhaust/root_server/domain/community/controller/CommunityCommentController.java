package io.github._3xhaust.root_server.domain.community.controller;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityCommentRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.ReactToCommentRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdateCommunityCommentRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityCommentListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityCommentResponse;
import io.github._3xhaust.root_server.domain.community.service.CommunityCommentService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Community Comment", description = "커뮤니티 댓글 관리 API")
@RestController
@RequestMapping("/api/v1/community/comments")
@RequiredArgsConstructor
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;

    @GetMapping
    public ApiResponse<Page<CommunityCommentListResponse>> getComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<CommunityCommentListResponse> comments = communityCommentService.getComments(postId, page, limit);
        return ApiResponse.ok(comments);
    }

    @GetMapping("/tree")
    public ApiResponse<List<CommunityCommentResponse>> getCommentsWithReplies(
            @RequestParam Long postId
    ) {
        List<CommunityCommentResponse> comments = communityCommentService.getCommentsWithReplies(postId);
        return ApiResponse.ok(comments);
    }

    @GetMapping("/{id}")
    public ApiResponse<CommunityCommentResponse> getCommentById(@PathVariable Long id) {
        CommunityCommentResponse comment = communityCommentService.getCommentById(id);
        return ApiResponse.ok(comment);
    }

    @PostMapping
    public ApiResponse<CommunityCommentResponse> createComment(
            Authentication authentication,
            @Valid @RequestBody CreateCommunityCommentRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CommunityCommentResponse comment = communityCommentService.createComment(userDetails.getUsername(), request);
        return ApiResponse.ok(HttpStatus.CREATED, comment);
    }

    @PutMapping("/{id}")
    public ApiResponse<CommunityCommentResponse> updateComment(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommunityCommentRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CommunityCommentResponse comment = communityCommentService.updateComment(userDetails.getUsername(), id, request);
        return ApiResponse.ok(comment);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(
            Authentication authentication,
            @PathVariable Long id
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        communityCommentService.deleteComment(userDetails.getUsername(), id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/reactions")
    public ApiResponse<Void> reactToComment(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ReactToCommentRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        communityCommentService.reactToComment(userDetails.getUsername(), id, request);
        return ApiResponse.ok(null);
    }
}
