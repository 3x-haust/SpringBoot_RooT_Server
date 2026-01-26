package io.github._3xhaust.root_server.domain.community.controller;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdateCommunityRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityResponse;
import io.github._3xhaust.root_server.domain.community.service.CommunityService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Community", description = "커뮤니티 관리 API")
@RestController
@RequestMapping("/api/v1/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping
    public ApiResponse<Page<CommunityListResponse>> getCommunities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<CommunityListResponse> communities = communityService.getCommunities(page, limit);
        return ApiResponse.ok(communities);
    }

    @GetMapping("/{id}")
    public ApiResponse<CommunityResponse> getCommunityById(@PathVariable Long id) {
        CommunityResponse community = communityService.getCommunityById(id);
        return ApiResponse.ok(community);
    }

    @PostMapping
    public ApiResponse<CommunityResponse> createCommunity(
            Authentication authentication,
            @Valid @RequestBody CreateCommunityRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CommunityResponse community = communityService.createCommunity(userDetails.getUsername(), request);
        return ApiResponse.ok(community);
    }

    @PutMapping("/{id}")
    public ApiResponse<CommunityResponse> updateCommunity(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommunityRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CommunityResponse community = communityService.updateCommunity(userDetails.getUsername(), id, request);
        return ApiResponse.ok(community);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCommunity(
            Authentication authentication,
            @PathVariable Long id
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        communityService.deleteCommunity(userDetails.getUsername(), id);
        return ApiResponse.ok(null);
    }
}
