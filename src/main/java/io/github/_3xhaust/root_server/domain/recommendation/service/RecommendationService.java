package io.github._3xhaust.root_server.domain.recommendation.service;

import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.repository.FavoriteGarageSaleRepository;
import io.github._3xhaust.root_server.domain.history.repository.SearchHistoryRepository;
import io.github._3xhaust.root_server.domain.history.repository.ViewHistoryRepository;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.repository.FavoriteUsedItemRepository;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.GarageSaleDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.GarageSaleSearchRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final GarageSaleSearchRepository garageSaleSearchRepository;
    private final ProductSearchRepository productSearchRepository;
    private final FavoriteGarageSaleRepository favoriteGarageSaleRepository;
    private final FavoriteUsedItemRepository favoriteUsedItemRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;
    private final io.github._3xhaust.root_server.infrastructure.elasticsearch.service.ElasticsearchTagService elasticsearchTagService;

    public Page<GarageSaleListResponse> recommendGarageSales(
            String userEmail,
            Double latitude,
            Double longitude,
            int page,
            int limit
    ) {
        Long userId = getUserId(userEmail);
        Instant thirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60);

        Set<Long> viewedIds = new HashSet<>();
        Set<Long> searchedIds = new HashSet<>();
        Set<Long> favoritedIds = new HashSet<>();

        if (userId != null) {
            viewedIds.addAll(viewHistoryRepository.findRecentGarageSaleIdsByUserId(userId, thirtyDaysAgo));
            searchedIds.addAll(searchHistoryRepository.findRecentGarageSaleIdsByUserId(userId, thirtyDaysAgo));
            favoritedIds.addAll(favoriteGarageSaleRepository.findGarageSalesByUserId(userId).stream()
                    .map(gs -> gs.getId())
                    .collect(Collectors.toSet()));
        }

        Set<Long> recommendedIds = new HashSet<>();
        recommendedIds.addAll(viewedIds);
        recommendedIds.addAll(searchedIds);
        recommendedIds.addAll(favoritedIds);

        if (recommendedIds.isEmpty()) {
            return new PageImpl<>(List.of(), PageRequest.of(page - 1, limit), 0);
        }

        List<Long> idList = new ArrayList<>(recommendedIds);
        Pageable pageable = PageRequest.of(page - 1, limit * 3);
        Page<GarageSaleDocument> documents = garageSaleSearchRepository.findRecommendedGarageSalesByIds(idList, pageable);

        List<GarageSaleDocument> sorted = documents.getContent().stream()
                .sorted((a, b) -> {
                    boolean aViewed = viewedIds.contains(a.getGarageSaleId());
                    boolean bViewed = viewedIds.contains(b.getGarageSaleId());
                    boolean aSearched = searchedIds.contains(a.getGarageSaleId());
                    boolean bSearched = searchedIds.contains(b.getGarageSaleId());
                    boolean aFavorited = favoritedIds.contains(a.getGarageSaleId());
                    boolean bFavorited = favoritedIds.contains(b.getGarageSaleId());

                    int aScore = (aFavorited ? 3 : 0) + (aSearched ? 2 : 0) + (aViewed ? 1 : 0);
                    int bScore = (bFavorited ? 3 : 0) + (bSearched ? 2 : 0) + (bViewed ? 1 : 0);

                    if (aScore != bScore) {
                        return Integer.compare(bScore, aScore);
                    }

                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());

        List<GarageSaleListResponse> responses = sorted.stream()
                .map(this::convertToGarageSaleListResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, PageRequest.of(page - 1, limit), documents.getTotalElements());
    }

    public Page<ProductListResponse> recommendProducts(
            String userEmail,
            Double latitude,
            Double longitude,
            int page,
            int limit
    ) {
        Long userId = getUserId(userEmail);
        Instant thirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60);

        Set<Long> viewedIds = new HashSet<>();
        Set<Long> searchedIds = new HashSet<>();
        Set<Long> favoritedIds = new HashSet<>();

        if (userId != null) {
            viewedIds.addAll(viewHistoryRepository.findRecentProductIdsByUserId(userId, thirtyDaysAgo));
            searchedIds.addAll(searchHistoryRepository.findRecentProductIdsByUserId(userId, thirtyDaysAgo));
            favoritedIds.addAll(favoriteUsedItemRepository.findProductsByUserId(userId).stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toSet()));
        }

        Set<Long> recommendedIds = new HashSet<>();
        recommendedIds.addAll(viewedIds);
        recommendedIds.addAll(searchedIds);
        recommendedIds.addAll(favoritedIds);

        if (recommendedIds.isEmpty()) {
            return new PageImpl<>(List.of(), PageRequest.of(page - 1, limit), 0);
        }

        List<Long> idList = new ArrayList<>(recommendedIds);
        Pageable pageable = PageRequest.of(page - 1, limit * 3);
        Page<ProductDocument> documents = productSearchRepository.findRecommendedProductsByIds((short) 0, idList, pageable);

        List<ProductDocument> sorted = documents.getContent().stream()
                .sorted((a, b) -> {
                    boolean aViewed = viewedIds.contains(a.getProductId());
                    boolean bViewed = viewedIds.contains(b.getProductId());
                    boolean aSearched = searchedIds.contains(a.getProductId());
                    boolean bSearched = searchedIds.contains(b.getProductId());
                    boolean aFavorited = favoritedIds.contains(a.getProductId());
                    boolean bFavorited = favoritedIds.contains(b.getProductId());

                    int aScore = (aFavorited ? 3 : 0) + (aSearched ? 2 : 0) + (aViewed ? 1 : 0);
                    int bScore = (bFavorited ? 3 : 0) + (bSearched ? 2 : 0) + (bViewed ? 1 : 0);

                    if (aScore != bScore) {
                        return Integer.compare(bScore, aScore);
                    }

                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());

        List<ProductListResponse> responses = sorted.stream()
                .map(this::convertToProductListResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, PageRequest.of(page - 1, limit), documents.getTotalElements());
    }

    public Page<GarageSaleListResponse> trendingGarageSales(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit * 3);
        Page<GarageSaleDocument> allGarageSales = garageSaleSearchRepository.findByIsActiveTrue(pageable);

        List<Long> ids = allGarageSales.getContent().stream()
                .map(GarageSaleDocument::getGarageSaleId)
                .collect(Collectors.toList());

        Map<Long, Long> favoriteCounts = new HashMap<>();
        Map<Long, Long> viewCounts = new HashMap<>();

        for (Long id : ids) {
            favoriteCounts.put(id, favoriteGarageSaleRepository.countByGarageSaleId(id));
            viewCounts.put(id, viewHistoryRepository.countViewersByGarageSaleId(id));
        }

        List<GarageSaleDocument> sorted = allGarageSales.getContent().stream()
                .sorted((a, b) -> {
                    Long aFav = favoriteCounts.getOrDefault(a.getGarageSaleId(), 0L);
                    Long bFav = favoriteCounts.getOrDefault(b.getGarageSaleId(), 0L);
                    Long aView = viewCounts.getOrDefault(a.getGarageSaleId(), 0L);
                    Long bView = viewCounts.getOrDefault(b.getGarageSaleId(), 0L);

                    long aScore = aFav * 2 + aView;
                    long bScore = bFav * 2 + bView;

                    if (aScore != bScore) {
                        return Long.compare(bScore, aScore);
                    }

                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());

        List<GarageSaleListResponse> responses = sorted.stream()
                .map(this::convertToGarageSaleListResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, PageRequest.of(page - 1, limit), allGarageSales.getTotalElements());
    }

    public Page<ProductListResponse> trendingProducts(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit * 3);
        Page<ProductDocument> allProducts = productSearchRepository.findByTypeAndIsActiveTrue((short) 0, pageable);

        List<Long> ids = allProducts.getContent().stream()
                .map(ProductDocument::getProductId)
                .collect(Collectors.toList());

        Map<Long, Long> favoriteCounts = new HashMap<>();
        Map<Long, Long> viewCounts = new HashMap<>();

        for (Long id : ids) {
            favoriteCounts.put(id, favoriteUsedItemRepository.countByProductId(id));
            viewCounts.put(id, viewHistoryRepository.countViewersByProductId(id));
        }

        List<ProductDocument> sorted = allProducts.getContent().stream()
                .sorted((a, b) -> {
                    Long aFav = favoriteCounts.getOrDefault(a.getProductId(), 0L);
                    Long bFav = favoriteCounts.getOrDefault(b.getProductId(), 0L);
                    Long aView = viewCounts.getOrDefault(a.getProductId(), 0L);
                    Long bView = viewCounts.getOrDefault(b.getProductId(), 0L);

                    long aScore = aFav * 2 + aView;
                    long bScore = bFav * 2 + bView;

                    if (aScore != bScore) {
                        return Long.compare(bScore, aScore);
                    }

                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());

        List<ProductListResponse> responses = sorted.stream()
                .map(this::convertToProductListResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, PageRequest.of(page - 1, limit), allProducts.getTotalElements());
    }

    public List<String> getPopularGarageSaleTags(int limit) {
        return elasticsearchTagService.getPopularGarageSaleTags(limit);
    }

    public List<String> getPopularProductTags(int limit) {
        return elasticsearchTagService.getPopularProductTags(limit);
    }

    private Long getUserId(String userEmail) {
        if (userEmail == null) {
            return null;
        }
        return userRepository.findByName(userEmail)
                .map(user -> user.getId())
                .orElse(null);
    }


    private GarageSaleListResponse convertToGarageSaleListResponse(GarageSaleDocument doc) {
        return GarageSaleListResponse.builder()
                .id(doc.getGarageSaleId())
                .name(doc.getName())
                .latitude(doc.getLatitude())
                .longitude(doc.getLongitude())
                .startDate(doc.getStartDate())
                .endDate(doc.getEndDate())
                .startTime(convertToLocalTime(doc.getStartTime()))
                .endTime(convertToLocalTime(doc.getEndTime()))
                .owner(GarageSaleListResponse.OwnerInfo.builder()
                        .id(doc.getOwnerId())
                        .name(doc.getOwnerName())
                        .build())
                .productCount(doc.getProductCount())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private ProductListResponse convertToProductListResponse(ProductDocument doc) {
        String thumbnailUrl = doc.getImageUrls() != null && !doc.getImageUrls().isEmpty()
                ? doc.getImageUrls().get(0) : null;

        return ProductListResponse.builder()
                .id(doc.getProductId())
                .title(doc.getTitle())
                .price(doc.getPrice())
                .description(doc.getDescription())
                .type(doc.getType())
                .thumbnailUrl(thumbnailUrl)
                .createdAt(doc.getCreatedAt())
                .seller(ProductListResponse.SellerInfo.builder()
                        .id(doc.getSellerId())
                        .name(doc.getSellerName())
                        .build())
                .build();
    }

    private LocalTime convertToLocalTime(Instant instant) {
        if (instant == null) {
            return LocalTime.now();
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalTime();
    }
}
