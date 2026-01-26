package io.github._3xhaust.root_server.domain.community.service;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdateCommunityRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityResponse;
import io.github._3xhaust.root_server.domain.community.entity.Community;
import io.github._3xhaust.root_server.domain.community.exception.CommunityErrorCode;
import io.github._3xhaust.root_server.domain.community.exception.CommunityException;
import io.github._3xhaust.root_server.domain.community.repository.CommunityRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityTagRepository;
import io.github._3xhaust.root_server.domain.tag.repository.TagRepository;
import io.github._3xhaust.root_server.domain.tag.entity.Tag;
import io.github._3xhaust.root_server.domain.community.entity.CommunityTag;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityTagRepository communityTagRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public Page<CommunityListResponse> getCommunities(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return communityRepository.findAll(pageable)
                .map(CommunityListResponse::of);
    }

    public CommunityResponse getCommunityById(Long id) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND, "id=" + id));
        CommunityResponse response = CommunityResponse.of(community);
        List<String> tagNames = communityTagRepository.findTagNamesByCommunityId(id);
        return CommunityResponse.builder()
                .id(response.getId())
                .owner(response.getOwner())
                .name(response.getName())
                .description(response.getDescription())
                .points(response.getPoints())
                .gradeLevel(response.getGradeLevel())
                .tags(tagNames)
                .createdAt(response.getCreatedAt())
                .build();
    }

    @Transactional
    public CommunityResponse createCommunity(String name, CreateCommunityRequest request) {
        User owner = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Community community = Community.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .points(0)
                .gradeLevel((short) 1)
                .build();

        Community savedCommunity = communityRepository.save(community);

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            addTagsToCommunity(savedCommunity, request.getTags());
        }

        return CommunityResponse.of(savedCommunity);
    }

    @Transactional
    public CommunityResponse updateCommunity(String name, Long id, UpdateCommunityRequest request) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND, "id=" + id));

        if (!community.getOwner().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "communityId=" + id);
        }

        community.update(request.getName(), request.getDescription());
        return CommunityResponse.of(community);
    }

    @Transactional
    public void deleteCommunity(String name, Long id) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Community community = communityRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND, "id=" + id));

        if (!community.getOwner().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "communityId=" + id);
        }

        communityRepository.delete(community);
    }

    @Transactional
    public void addTagsToCommunity(Community community, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        List<Tag> tags = getOrCreateTags(tagNames, "COMMUNITY");
        for (Tag tag : tags) {
            CommunityTag communityTag = CommunityTag.builder()
                    .community(community)
                    .tag(tag)
                    .build();
            communityTagRepository.save(communityTag);
        }
    }

    @Transactional
    public void updateCommunityTags(Community community, List<String> tagNames) {
        communityTagRepository.deleteByCommunityId(community.getId());
        addTagsToCommunity(community, tagNames);
    }

    private List<Tag> getOrCreateTags(List<String> tagNames, String category) {
        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .name(tagName)
                            .category(category)
                            .build()));
            tags.add(tag);
        }
        return tags;
    }
}
