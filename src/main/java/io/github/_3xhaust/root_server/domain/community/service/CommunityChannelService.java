package io.github._3xhaust.root_server.domain.community.service;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityChannelRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdateCommunityChannelRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityChannelResponse;
import io.github._3xhaust.root_server.domain.community.entity.Community;
import io.github._3xhaust.root_server.domain.community.entity.CommunityChannel;
import io.github._3xhaust.root_server.domain.community.exception.CommunityErrorCode;
import io.github._3xhaust.root_server.domain.community.exception.CommunityException;
import io.github._3xhaust.root_server.domain.community.repository.CommunityChannelRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityChannelService {

    private final CommunityChannelRepository communityChannelRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public List<CommunityChannelResponse> getChannels(Long communityId) {
        List<CommunityChannel> channels = communityChannelRepository.findByCommunityId(communityId);
        return channels.stream()
                .map(CommunityChannelResponse::of)
                .toList();
    }

    public List<CommunityChannelResponse> getChannelsByType(Long communityId, String type) {
        List<CommunityChannel> channels = communityChannelRepository.findByCommunityIdAndType(communityId, type);
        return channels.stream()
                .map(CommunityChannelResponse::of)
                .toList();
    }

    public CommunityChannelResponse getChannelById(Long id) {
        CommunityChannel channel = communityChannelRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND, "channelId=" + id));
        return CommunityChannelResponse.of(channel);
    }

    @Transactional
    public CommunityChannelResponse createChannel(String name, CreateCommunityChannelRequest request) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Community community = communityRepository.findById(request.getCommunityId())
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND, "id=" + request.getCommunityId()));

        if (!community.getOwner().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "communityId=" + request.getCommunityId());
        }

        CommunityChannel channel = CommunityChannel.builder()
                .community(community)
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .build();

        CommunityChannel savedChannel = communityChannelRepository.save(channel);
        return CommunityChannelResponse.of(savedChannel);
    }

    @Transactional
    public CommunityChannelResponse updateChannel(String name, Long id, UpdateCommunityChannelRequest request) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityChannel channel = communityChannelRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND, "channelId=" + id));

        if (!channel.getCommunity().getOwner().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "channelId=" + id);
        }

        channel.update(request.getName(), request.getDescription());
        return CommunityChannelResponse.of(channel);
    }

    @Transactional
    public void deleteChannel(String name, Long id) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityChannel channel = communityChannelRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND, "channelId=" + id));

        if (!channel.getCommunity().getOwner().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "channelId=" + id);
        }

        communityChannelRepository.delete(channel);
    }
}
