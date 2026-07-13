package io.github._3xhaust.root_server.domain.recommendation.service;

import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.repository.GarageSaleRepository;
import io.github._3xhaust.root_server.domain.garagesale.repository.FavoriteGarageSaleRepository;
import io.github._3xhaust.root_server.domain.history.repository.SearchHistoryRepository;
import io.github._3xhaust.root_server.domain.history.repository.ViewHistoryRepository;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.repository.FavoriteUsedItemRepository;
import io.github._3xhaust.root_server.domain.product.repository.ProductRepository;
import io.github._3xhaust.root_server.domain.tag.repository.GarageSaleTagRepository;
import io.github._3xhaust.root_server.domain.tag.repository.ProductTagRepository;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.GarageSaleDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.GarageSaleSearchRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RecommendationService {

    private final GarageSaleRepository garageSaleRepository;
    private final GarageSaleSearchRepository garageSaleSearchRepository;
    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final FavoriteGarageSaleRepository favoriteGarageSaleRepository;
    private final FavoriteUsedItemRepository favoriteUsedItemRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final GarageSaleTagRepository garageSaleTagRepository;
    private final ProductTagRepository productTagRepository;
    private final UserRepository userRepository;
    private final io.github._3xhaust.root_server.infrastructure.elasticsearch.service.ElasticsearchTagService elasticsearchTagService;
    private static final double DEFAULT_PRODUCT_RADIUS_KM = 10.0;
    private static final double DEFAULT_GARAGE_RADIUS_KM = 15.0;

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
            if (hasLocation(latitude, longitude)) {
                try {
                    Pageable nearbyPageable = PageRequest.of(page - 1, limit);
                    Page<GarageSaleListResponse> nearby = garageSaleSearchRepository.findByLocationNear(latitude, longitude, DEFAULT_GARAGE_RADIUS_KM, nearbyPageable)
                            .map(this::convertToGarageSaleListResponse);
                    if (nearby.hasContent()) {
                        return nearby;
                    }
                } catch (RuntimeException e) {
                    log.warn("Garage sale recommendation search failed; falling back to database", e);
                }
            }
            return trendingGarageSales(page, limit);
        }

        try {
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

            if (sorted.isEmpty()) {
                return trendingGarageSales(page, limit);
            }

            List<GarageSaleListResponse> responses = sorted.stream()
                    .map(this::convertToGarageSaleListResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, PageRequest.of(page - 1, limit), documents.getTotalElements());
        } catch (RuntimeException e) {
            log.warn("Garage sale recommendation search failed; falling back to database", e);
            return trendingGarageSales(page, limit);
        }
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
            if (hasLocation(latitude, longitude)) {
                try {
                    Pageable nearbyPageable = PageRequest.of(page - 1, limit);
                    Page<ProductListResponse> nearby = productSearchRepository.findByTypeAndLocationNear((short) 0, latitude, longitude, DEFAULT_PRODUCT_RADIUS_KM, nearbyPageable)
                            .map(this::convertToProductListResponse);
                    if (nearby.hasContent()) {
                        return nearby;
                    }
                } catch (RuntimeException e) {
                    log.warn("Product recommendation search failed; falling back to database", e);
                }
            }
            return trendingProducts(page, limit);
        }

        try {
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

            if (sorted.isEmpty()) {
                return trendingProducts(page, limit);
            }

            List<ProductListResponse> responses = sorted.stream()
                    .map(this::convertToProductListResponse)
                    .collect(Collectors.toList());

            return new PageImpl<>(responses, PageRequest.of(page - 1, limit), documents.getTotalElements());
        } catch (RuntimeException e) {
            log.warn("Product recommendation search failed; falling back to database", e);
            return trendingProducts(page, limit);
        }
    }

    public Page<GarageSaleListResponse> trendingGarageSales(int page, int limit) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit * 3);
            Page<GarageSaleDocument> allGarageSales = garageSaleSearchRepository.findByIsActiveTrue(pageable);

            if (!allGarageSales.hasContent()) {
                return fallbackTrendingGarageSales(page, limit);
            }

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
        } catch (RuntimeException e) {
            log.warn("Trending garage sale search failed; falling back to database", e);
            return fallbackTrendingGarageSales(page, limit);
        }
    }

    public Page<ProductListResponse> trendingProducts(int page, int limit) {
        try {
            Pageable pageable = PageRequest.of(page - 1, limit * 3);
            Page<ProductDocument> allProducts = productSearchRepository.findByTypeAndIsActiveTrue((short) 0, pageable);

            if (!allProducts.hasContent()) {
                return fallbackTrendingProducts(page, limit);
            }

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
        } catch (RuntimeException e) {
            log.warn("Trending product search failed; falling back to database", e);
            return fallbackTrendingProducts(page, limit);
        }
    }

    public List<String> getPopularGarageSaleTags(int limit) {
        try {
            List<String> tags = elasticsearchTagService.getPopularGarageSaleTags(limit);
            if (!tags.isEmpty()) {
                return tags;
            }
        } catch (RuntimeException e) {
            log.warn("Popular garage sale tags search failed; falling back to database", e);
        }
        return topTags(garageSaleTagRepository.findAllTagNames(), limit);
    }

    public List<String> getPopularProductTags(int limit) {
        try {
            List<String> tags = elasticsearchTagService.getPopularProductTags(limit);
            if (!tags.isEmpty()) {
                return tags;
            }
        } catch (RuntimeException e) {
            log.warn("Popular product tags search failed; falling back to database", e);
        }
        return topTags(productTagRepository.findAllTagNames(), limit);
    }

    private Page<GarageSaleListResponse> fallbackTrendingGarageSales(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return garageSaleRepository.findAll(pageable).map(GarageSaleListResponse::of);
    }

    private Page<ProductListResponse> fallbackTrendingProducts(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productRepository.findByType((short) 0, pageable).map(ProductListResponse::of);
    }

    private List<String> topTags(List<String> tagNames, int limit) {
        return tagNames.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(tagName -> tagName, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((a, b) -> {
                    int byCount = Long.compare(b.getValue(), a.getValue());
                    if (byCount != 0) {
                        return byCount;
                    }
                    return a.getKey().compareToIgnoreCase(b.getKey());
                })
                .map(Map.Entry::getKey)
                .limit(Math.max(limit, 0))
                .toList();
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
                .tags(doc.getTags())
                .imageUrls(doc.getImageUrls())
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
                .latitude(doc.getLatitude())
                .longitude(doc.getLongitude())
                .createdAt(doc.getCreatedAt())
                .seller(ProductListResponse.SellerInfo.builder()
                        .id(doc.getSellerId())
                        .name(doc.getSellerName())
                        .build())
                .tags(doc.getTags())
                .build();
    }

    private LocalTime convertToLocalTime(Instant instant) {
        if (instant == null) {
            return LocalTime.now();
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalTime();
    }

    private boolean hasLocation(Double latitude, Double longitude) {
        return latitude != null && longitude != null
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }
}
