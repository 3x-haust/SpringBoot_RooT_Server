package io.github._3xhaust.root_server.domain.garagesale.service;

import io.github._3xhaust.root_server.domain.garagesale.dto.req.CreateGarageSaleRequest;
import io.github._3xhaust.root_server.domain.garagesale.dto.req.UpdateGarageSaleRequest;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleResponse;
import io.github._3xhaust.root_server.domain.garagesale.entity.FavoriteGarageSale;
import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import io.github._3xhaust.root_server.domain.garagesale.exception.GarageSaleErrorCode;
import io.github._3xhaust.root_server.domain.garagesale.exception.GarageSaleException;
import io.github._3xhaust.root_server.domain.garagesale.repository.FavoriteGarageSaleRepository;
import io.github._3xhaust.root_server.domain.garagesale.repository.GarageSaleRepository;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductResponse;
import io.github._3xhaust.root_server.domain.product.entity.FavoriteUsedItem;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.product.exception.ProductErrorCode;
import io.github._3xhaust.root_server.domain.product.exception.ProductException;
import io.github._3xhaust.root_server.domain.product.repository.FavoriteUsedItemRepository;
import io.github._3xhaust.root_server.domain.product.repository.ProductRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleDetailResponse;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.GarageSaleDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.GarageSaleSearchRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GarageSaleService {

    private final GarageSaleRepository garageSaleRepository;
    private final FavoriteGarageSaleRepository favoriteGarageSaleRepository;
    private final ProductRepository productRepository;
    private final FavoriteUsedItemRepository favoriteUsedItemRepository;
    private final UserRepository userRepository;
    private final GarageSaleSearchRepository garageSaleSearchRepository;
    private final ProductSearchRepository productSearchRepository;

    public List<GarageSaleListResponse> getAllGarageSales(Double lat, Double lng, Double radius) {
        List<GarageSale> garageSales;

        if (lat != null && lng != null && radius != null) {
            garageSales = garageSaleRepository.findNearbyGarageSales(lat, lng, radius);
        } else {
            garageSales = garageSaleRepository.findAll();
        }

        return garageSales.stream()
                .map(GarageSaleListResponse::of)
                .toList();
    }

    public GarageSaleResponse getGarageSaleById(Long id) {
        GarageSale garageSale = garageSaleRepository.findById(id)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + id));
        return GarageSaleResponse.of(garageSale);
    }

    @Transactional
    public GarageSaleResponse createGarageSale(String name, CreateGarageSaleRequest request) {
        User owner = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        GarageSale garageSale = GarageSale.builder()
                .owner(owner)
                .name(request.getName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        GarageSale savedGarageSale = garageSaleRepository.save(garageSale);
        return GarageSaleResponse.ofWithoutProducts(savedGarageSale);
    }

    @Transactional
    public GarageSaleResponse updateGarageSale(String name, Long id, UpdateGarageSaleRequest request) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        GarageSale garageSale = garageSaleRepository.findById(id)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + id));

        if (!garageSale.getOwner().getId().equals(user.getId())) {
            throw new GarageSaleException(GarageSaleErrorCode.UNAUTHORIZED_ACCESS, "garageSaleId=" + id);
        }

        garageSale.update(
                request.getName() != null ? request.getName() : garageSale.getName(),
                request.getLatitude() != null ? request.getLatitude() : garageSale.getLatitude(),
                request.getLongitude() != null ? request.getLongitude() : garageSale.getLongitude(),
                request.getStartTime() != null ? request.getStartTime() : garageSale.getStartTime(),
                request.getEndTime() != null ? request.getEndTime() : garageSale.getEndTime()
        );

        return GarageSaleResponse.of(garageSale);
    }

    @Transactional
    public void toggleFavoriteGarageSale(String name, Long garageSaleId) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        GarageSale garageSale = garageSaleRepository.findById(garageSaleId)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + garageSaleId));

        if (favoriteGarageSaleRepository.existsByUserIdAndGarageSaleId(user.getId(), garageSaleId)) {
            favoriteGarageSaleRepository.deleteByUserIdAndGarageSaleId(user.getId(), garageSaleId);
        } else {
            FavoriteGarageSale favorite = FavoriteGarageSale.builder()
                    .user(user)
                    .garageSale(garageSale)
                    .build();
            favoriteGarageSaleRepository.save(favorite);
        }
    }

    public ProductResponse getGarageSaleProduct(Long garageSaleId, Long productId) {
        GarageSale garageSale = garageSaleRepository.findById(garageSaleId)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + garageSaleId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (product.getGarageSale() == null || !product.getGarageSale().getId().equals(garageSaleId)) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND,
                    "Product " + productId + " not found in garage sale " + garageSaleId);
        }

        return ProductResponse.of(product);
    }

    @Transactional
    public void toggleFavoriteGarageSaleProduct(String name, Long garageSaleId, Long productId) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        GarageSale garageSale = garageSaleRepository.findById(garageSaleId)
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + garageSaleId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (product.getGarageSale() == null || !product.getGarageSale().getId().equals(garageSaleId)) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND,
                    "Product " + productId + " not found in garage sale " + garageSaleId);
        }

        if (favoriteUsedItemRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            favoriteUsedItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
        } else {
            FavoriteUsedItem favorite = FavoriteUsedItem.builder()
                    .user(user)
                    .product(product)
                    .build();
            favoriteUsedItemRepository.save(favorite);
        }
    }

    public List<GarageSaleListResponse> getFavoriteGarageSales(String name) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        List<GarageSale> garageSales = favoriteGarageSaleRepository.findGarageSalesByUserId(user.getId());
        return garageSales.stream()
                .map(GarageSaleListResponse::of)
                .toList();
    }

    public Page<GarageSaleListResponse> getGarageSalesFromElasticsearch(int page, int limit, String sortBy, String sortDir) {
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return garageSaleSearchRepository.findByIsActiveTrue(pageable)
                .map(this::convertToGarageSaleListResponse);
    }

    public GarageSaleDetailResponse getGarageSaleDetailFromElasticsearch(Long garageSaleId, int productPage, int productLimit) {
        GarageSaleDocument garageSaleDoc = garageSaleSearchRepository
                .findById(GarageSaleDocument.generateId(garageSaleId))
                .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND, "id=" + garageSaleId));

        Pageable pageable = PageRequest.of(productPage - 1, productLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductDocument> productPage2 = productSearchRepository.findByGarageSaleIdAndIsActiveTrue(garageSaleId, pageable);

        return GarageSaleDetailResponse.builder()
                .garageSale(convertToGarageSaleListResponse(garageSaleDoc))
                .products(productPage2.getContent().stream()
                        .map(this::convertToProductListResponse)
                        .toList())
                .pageInfo(GarageSaleDetailResponse.PageInfo.builder()
                        .currentPage(productPage)
                        .totalPages(productPage2.getTotalPages())
                        .totalElements(productPage2.getTotalElements())
                        .hasNext(productPage2.hasNext())
                        .build())
                .build();
    }

    public Page<GarageSaleListResponse> searchNearbyGarageSalesFromElasticsearch(Double latitude, Double longitude,
                                                                                    Double radiusKm, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        double radius = radiusKm != null ? radiusKm : 10.0;

        return garageSaleSearchRepository.findByLocationNear(latitude, longitude, radius, pageable)
                .map(doc -> {
                    double distance = calculateDistance(latitude, longitude, doc.getLatitude(), doc.getLongitude());
                    GarageSaleListResponse response = convertToGarageSaleListResponse(doc);
                    return response;
                });
    }

    public Page<GarageSaleListResponse> getCurrentGarageSalesFromElasticsearch(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "endTime"));
        return garageSaleSearchRepository.findCurrentlyActive(Instant.now(), pageable)
                .map(this::convertToGarageSaleListResponse);
    }

    public Page<GarageSaleListResponse> getGarageSalesByDateRangeFromElasticsearch(Instant startDate, Instant endDate,
                                                                                      int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "startTime"));
        return garageSaleSearchRepository.findByDateRange(startDate, endDate, pageable)
                .map(this::convertToGarageSaleListResponse);
    }

    public Page<GarageSaleListResponse> getGarageSalesByTagFromElasticsearch(String tag, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return garageSaleSearchRepository.findByTagsContainingAndIsActiveTrue(tag, pageable)
                .map(this::convertToGarageSaleListResponse);
    }

    public Page<GarageSaleListResponse> searchGarageSalesByKeyword(String keyword, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return garageSaleSearchRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToGarageSaleListResponse);
    }

    private GarageSaleListResponse convertToGarageSaleListResponse(GarageSaleDocument document) {
        return GarageSaleListResponse.builder()
                .id(document.getGarageSaleId())
                .name(document.getName())
                .latitude(document.getLatitude())
                .longitude(document.getLongitude())
                .startTime(document.getStartTime())
                .endTime(document.getEndTime())
                .owner(GarageSaleListResponse.OwnerInfo.builder()
                        .id(document.getOwnerId())
                        .name(document.getOwnerName())
                        .build())
                .productCount(document.getProductCount())
                .createdAt(document.getCreatedAt())
                .build();
    }

    private ProductListResponse convertToProductListResponse(ProductDocument document) {
        String thumbnailUrl = document.getImageUrls() != null && !document.getImageUrls().isEmpty()
                ? document.getImageUrls().get(0) : null;

        return ProductListResponse.builder()
                .id(document.getProductId())
                .title(document.getTitle())
                .price(document.getPrice())
                .description(document.getDescription())
                .type(document.getType())
                .thumbnailUrl(thumbnailUrl)
                .createdAt(document.getCreatedAt())
                .seller(ProductListResponse.SellerInfo.builder()
                        .id(document.getSellerId())
                        .name(document.getSellerName())
                        .build())
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Sort createSort(String sortBy, String sortDir) {
        String field = sortBy != null ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}

