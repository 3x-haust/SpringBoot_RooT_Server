package io.github._3xhaust.root_server.domain.community.controller;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityChannelRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdateCommunityChannelRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityChannelResponse;
import io.github._3xhaust.root_server.domain.community.service.CommunityChannelService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Community Channel", description = "커뮤니티 게시판 관리 API")
@RestController
@RequestMapping("/api/v1/community/channels")
@RequiredArgsConstructor
public class CommunityChannelController {

    private final CommunityChannelService communityChannelService;

    @GetMapping
    public ApiResponse<List<CommunityChannelResponse>> getChannels(
            @RequestParam Long communityId,
            @RequestParam(required = false) String type
    ) {
        List<CommunityChannelResponse> channels;
        if (type != null) {
            channels = communityChannelService.getChannelsByType(communityId, type);
        } else {
            channels = communityChannelService.getChannels(communityId);
        }
        return ApiResponse.ok(channels);
    }

    @GetMapping("/{id}")
    public ApiResponse<CommunityChannelResponse> getChannelById(@PathVariable Long id) {
        CommunityChannelResponse channel = communityChannelService.getChannelById(id);
        return ApiResponse.ok(channel);
    }

    @PostMapping
    public ApiResponse<CommunityChannelResponse> createChannel(
            Authentication authentication,
            @Valid @RequestBody CreateCommunityChannelRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CommunityChannelResponse channel = communityChannelService.createChannel(userDetails.getUsername(), request);
        return ApiResponse.ok(HttpStatus.CREATED, channel);
    }

    @PutMapping("/{id}")
    public ApiResponse<CommunityChannelResponse> updateChannel(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateCommunityChannelRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CommunityChannelResponse channel = communityChannelService.updateChannel(userDetails.getUsername(), id, request);
        return ApiResponse.ok(channel);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteChannel(
            Authentication authentication,
            @PathVariable Long id
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        communityChannelService.deleteChannel(userDetails.getUsername(), id);
        return ApiResponse.ok(null);
    }
}
