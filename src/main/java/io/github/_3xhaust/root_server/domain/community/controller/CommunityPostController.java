package io.github._3xhaust.root_server.domain.community.controller;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityPostRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.ReactToPostRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdateCommunityPostRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityPostListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityPostResponse;
import io.github._3xhaust.root_server.domain.community.service.CommunityPostService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Community Post", description = "커뮤니티 게시글 관리 API")
@RestController
@RequestMapping("/api/v1/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @GetMapping
    public ApiResponse<Page<CommunityPostListResponse>> getPosts(
            @RequestParam Long channelId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<CommunityPostListResponse> posts = communityPostService.getPosts(channelId, page, limit);
        return ApiResponse.ok(posts);
    }

    @GetMapping("/{id}")
    public ApiResponse<CommunityPostResponse> getPostById(@PathVariable Long id) {
        CommunityPostResponse post = communityPostService.getPostById(id);
        return ApiResponse.ok(post);
    }

    @PostMapping
    public ApiResponse<CommunityPostResponse> createPost(
            Authentication authentication,
            @Valid @RequestBody CreateCommunityPostRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CommunityPostResponse post = communityPostService.createPost(userDetails.getUsername(), request);
        return ApiResponse.ok(HttpStatus.CREATED, post);
    }

    @PutMapping("/{id}")
    public ApiResponse<CommunityPostResponse> updatePost(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommunityPostRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CommunityPostResponse post = communityPostService.updatePost(userDetails.getUsername(), id, request);
        return ApiResponse.ok(post);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(
            Authentication authentication,
            @PathVariable Long id
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        communityPostService.deletePost(userDetails.getUsername(), id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/reactions")
    public ApiResponse<Void> reactToPost(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ReactToPostRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        communityPostService.reactToPost(userDetails.getUsername(), id, request);
        return ApiResponse.ok(null);
    }
}
