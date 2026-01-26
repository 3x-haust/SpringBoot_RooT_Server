package io.github._3xhaust.root_server.domain.community.service;

import io.github._3xhaust.root_server.domain.community.dto.req.CreateCommunityPostRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.ReactToPostRequest;
import io.github._3xhaust.root_server.domain.community.dto.req.UpdateCommunityPostRequest;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityPostListResponse;
import io.github._3xhaust.root_server.domain.community.dto.res.CommunityPostResponse;
import io.github._3xhaust.root_server.domain.community.entity.CommunityChannel;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPost;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPostImage;
import io.github._3xhaust.root_server.domain.community.entity.CommunityPostReaction;
import io.github._3xhaust.root_server.domain.community.exception.CommunityErrorCode;
import io.github._3xhaust.root_server.domain.community.exception.CommunityException;
import io.github._3xhaust.root_server.domain.community.repository.CommunityPostImageRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityPostReactionRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityChannelRepository;
import io.github._3xhaust.root_server.domain.community.repository.CommunityPostRepository;
import io.github._3xhaust.root_server.domain.image.entity.Image;
import io.github._3xhaust.root_server.domain.image.repository.ImageRepository;
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

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityPostService {

    private final CommunityPostRepository communityPostRepository;
    private final CommunityPostImageRepository communityPostImageRepository;
    private final CommunityPostReactionRepository communityPostReactionRepository;
    private final CommunityChannelRepository communityChannelRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public Page<CommunityPostListResponse> getPosts(Long channelId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return communityPostRepository.findByChannelId(channelId, pageable)
                .map(CommunityPostListResponse::of);
    }

    public CommunityPostResponse getPostById(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND, "id=" + id));
        return CommunityPostResponse.of(post);
    }

    @Transactional
    public CommunityPostResponse createPost(String name, CreateCommunityPostRequest request) {
        User author = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityChannel channel = communityChannelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_NOT_FOUND, "channelId=" + request.getChannelId()));

        CommunityPost post = CommunityPost.builder()
                .channel(channel)
                .author(author)
                .title(request.getTitle())
                .body(request.getBody())
                .build();

        CommunityPost savedPost = communityPostRepository.save(post);

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            for (Long imageId : request.getImageIds()) {
                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
                CommunityPostImage postImage = CommunityPostImage.builder()
                        .post(savedPost)
                        .image(image)
                        .build();
                communityPostImageRepository.save(postImage);
                savedPost.addImage(postImage);
            }
        }

        channel.getCommunity().addPoints(10);
        return CommunityPostResponse.of(savedPost);
    }

    @Transactional
    public CommunityPostResponse updatePost(String name, Long id, UpdateCommunityPostRequest request) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND, "id=" + id));

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "postId=" + id);
        }

        post.update(request.getTitle(), request.getBody());

        if (request.getImageIds() != null) {
            post.clearImages();
            communityPostImageRepository.deleteByPostId(id);

            for (Long imageId : request.getImageIds()) {
                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
                CommunityPostImage postImage = CommunityPostImage.builder()
                        .post(post)
                        .image(image)
                        .build();
                communityPostImageRepository.save(postImage);
                post.addImage(postImage);
            }
        }

        return CommunityPostResponse.of(post);
    }

    @Transactional
    public void deletePost(String name, Long id) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND, "id=" + id));

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new CommunityException(CommunityErrorCode.UNAUTHORIZED_ACCESS, "postId=" + id);
        }

        communityPostRepository.delete(post);
    }

    @Transactional
    public void reactToPost(String name, Long postId, ReactToPostRequest request) {
        if (request.getReaction() != 1 && request.getReaction() != -1) {
            throw new CommunityException(CommunityErrorCode.INVALID_REACTION, "reaction=" + request.getReaction());
        }

        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMUNITY_POST_NOT_FOUND, "id=" + postId));

        Optional<CommunityPostReaction> existingReaction = communityPostReactionRepository
                .findByUserIdAndPostIdWithEntities(user.getId(), postId);

        if (existingReaction.isPresent()) {
            CommunityPostReaction reaction = existingReaction.get();
            if (reaction.getReaction().equals(request.getReaction())) {
                communityPostReactionRepository.delete(reaction);
            } else {
                reaction.updateReaction(request.getReaction());
            }
        } else {
            CommunityPostReaction reaction = CommunityPostReaction.builder()
                    .user(user)
                    .post(post)
                    .reaction(request.getReaction())
                    .build();
            communityPostReactionRepository.save(reaction);
        }
    }
}
