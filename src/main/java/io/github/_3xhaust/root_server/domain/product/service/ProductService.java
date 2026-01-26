package io.github._3xhaust.root_server.domain.product.service;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import io.github._3xhaust.root_server.domain.garagesale.exception.GarageSaleErrorCode;
import io.github._3xhaust.root_server.domain.garagesale.exception.GarageSaleException;
import io.github._3xhaust.root_server.domain.garagesale.repository.GarageSaleRepository;
import io.github._3xhaust.root_server.domain.image.entity.Image;
import io.github._3xhaust.root_server.domain.image.repository.ImageRepository;
import io.github._3xhaust.root_server.domain.product.dto.req.CreateProductRequest;
import io.github._3xhaust.root_server.domain.product.dto.req.UpdateProductRequest;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductResponse;
import io.github._3xhaust.root_server.domain.product.entity.FavoriteUsedItem;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import io.github._3xhaust.root_server.domain.product.entity.ProductImage;
import io.github._3xhaust.root_server.domain.product.exception.ProductErrorCode;
import io.github._3xhaust.root_server.domain.product.exception.ProductException;
import io.github._3xhaust.root_server.domain.product.repository.FavoriteUsedItemRepository;
import io.github._3xhaust.root_server.domain.product.repository.ProductImageRepository;
import io.github._3xhaust.root_server.domain.product.repository.ProductRepository;
import io.github._3xhaust.root_server.domain.user.entity.User;
import io.github._3xhaust.root_server.domain.user.exception.UserErrorCode;
import io.github._3xhaust.root_server.domain.user.exception.UserException;
import io.github._3xhaust.root_server.domain.user.repository.UserRepository;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.document.ProductDocument;
import io.github._3xhaust.root_server.infrastructure.elasticsearch.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FavoriteUsedItemRepository favoriteUsedItemRepository;
    private final UserRepository userRepository;
    private final GarageSaleRepository garageSaleRepository;
    private final ImageRepository imageRepository;
    private final ProductSearchRepository productSearchRepository;

    private static final short TYPE_USED = 0;

    public Page<ProductListResponse> getProducts(Short type, int page, int limit, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> products;
        if (type != null) {
            products = productRepository.findByType(type, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(product -> {
            Boolean isFavorite = userId != null && favoriteUsedItemRepository.existsByUserIdAndProductId(userId, product.getId());
            return ProductListResponse.of(product, isFavorite);
        });
    }

    public ProductResponse getProductById(Long productId, Long userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));
        Boolean isFavorite = userId != null && favoriteUsedItemRepository.existsByUserIdAndProductId(userId, productId);
        return ProductResponse.of(product, isFavorite);
    }

    @Transactional
    public ProductResponse createProduct(String name, CreateProductRequest request) {
        User seller = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        GarageSale garageSale = null;
        if (request.getGarageSaleId() != null) {
            garageSale = garageSaleRepository.findById(request.getGarageSaleId())
                    .orElseThrow(() -> new GarageSaleException(GarageSaleErrorCode.GARAGE_SALE_NOT_FOUND,
                            "id=" + request.getGarageSaleId()));
        }

        Product product = Product.builder()
                .seller(seller)
                .title(request.getTitle())
                .price(request.getPrice())
                .description(request.getDescription())
                .body(request.getBody())
                .type(request.getType())
                .garageSale(garageSale)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Product savedProduct = productRepository.save(product);

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            for (Long imageId : request.getImageIds()) {
                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
                ProductImage productImage = ProductImage.builder()
                        .product(savedProduct)
                        .image(image)
                        .build();
                productImageRepository.save(productImage);
                savedProduct.addImage(productImage);
            }
        }

        Boolean isFavorite = favoriteUsedItemRepository.existsByUserIdAndProductId(seller.getId(), savedProduct.getId());
        return ProductResponse.of(savedProduct, isFavorite);
    }

    @Transactional
    public ProductResponse updateProduct(String name, Long productId, UpdateProductRequest request) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new ProductException(ProductErrorCode.UNAUTHORIZED_ACCESS, "productId=" + productId);
        }

        product.update(
                request.getTitle() != null ? request.getTitle() : product.getTitle(),
                request.getPrice() != null ? request.getPrice() : product.getPrice(),
                request.getDescription() != null ? request.getDescription() : product.getDescription(),
                request.getBody() != null ? request.getBody() : product.getBody(),
                request.getLatitude() != null ? request.getLatitude() : product.getLatitude(),
                request.getLongitude() != null ? request.getLongitude() : product.getLongitude()
        );

        if (request.getImageIds() != null) {
            productImageRepository.deleteByProductId(productId);
            product.clearImages();

            for (Long imageId : request.getImageIds()) {
                Image image = imageRepository.findById(imageId)
                        .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
                ProductImage productImage = ProductImage.builder()
                        .product(product)
                        .image(image)
                        .build();
                productImageRepository.save(productImage);
                product.addImage(productImage);
            }
        }

        Boolean isFavorite = favoriteUsedItemRepository.existsByUserIdAndProductId(user.getId(), product.getId());
        return ProductResponse.of(product, isFavorite);
    }

    @Transactional
    public void deleteProduct(String name, Long productId) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new ProductException(ProductErrorCode.UNAUTHORIZED_ACCESS, "productId=" + productId);
        }

        productRepository.delete(product);
    }

    @Transactional
    public void toggleFavorite(String name, Long productId) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

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

    public List<ProductListResponse> getFavoriteProducts(String name) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        List<Product> products = favoriteUsedItemRepository.findProductsByUserId(user.getId());
        return products.stream()
                .map(ProductListResponse::of)
                .toList();
    }

    public Page<ProductListResponse> searchProducts(String title, Double minPrice, Double maxPrice, int page, int limit, String sortBy, String direction, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<Product> products = productRepository.searchProducts(title, minPrice, maxPrice, pageable);
        return products.map(product -> {
            Boolean isFavorite = userId != null && favoriteUsedItemRepository.existsByUserIdAndProductId(userId, product.getId());
            return ProductListResponse.of(product, isFavorite);
        });
    }

    @Transactional
    public void uploadProductImages(String name, Long productId, List<Long> imageIds) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new ProductException(ProductErrorCode.UNAUTHORIZED_ACCESS, "productId=" + productId);
        }

        imageIds.stream().distinct().forEach(imageId -> {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));
            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .image(image)
                    .build();
            productImageRepository.save(productImage);
            product.addImage(productImage);
        });
    }

    @Transactional
    public void deleteProductImage(String name, Long productId, Long imageId) {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "name=" + name));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new ProductException(ProductErrorCode.UNAUTHORIZED_ACCESS, "productId=" + productId);
        }

        ProductImage productImage = productImageRepository.findByProductIdAndImageId(productId, imageId);
        if (productImage == null) {
            throw new IllegalArgumentException("Image not found for product: " + imageId);
        }
        productImageRepository.delete(productImage);
        product.removeImage(productImage);
    }

    public Page<ProductListResponse> getUsedProductsFromElasticsearch(int page, int limit, String sortBy, String sortDir, Long userId) {
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        return productSearchRepository.findByTypeAndIsActiveTrue(TYPE_USED, pageable)
                .map(doc -> convertToProductListResponse(doc, userId));
    }

    public Page<ProductListResponse> searchUsedProductsFromElasticsearch(String keyword, int page, int limit,
                                                                          Integer minPrice, Integer maxPrice, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProductDocument> result;
        if (minPrice != null && maxPrice != null) {
            result = productSearchRepository.findByPriceRangeAndType(minPrice, maxPrice, TYPE_USED, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            result = productSearchRepository.searchByKeywordAndType(keyword, TYPE_USED, pageable);
        } else {
            result = productSearchRepository.findByTypeAndIsActiveTrue(TYPE_USED, pageable);
        }

        return result.map(doc -> convertToProductListResponse(doc, userId));
    }

    public Page<ProductListResponse> getProductsByTagFromElasticsearch(String tag, int page, int limit, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productSearchRepository.findByTagsContainingAndIsActiveTrue(tag, pageable)
                .map(doc -> convertToProductListResponse(doc, userId));
    }

    public Page<ProductListResponse> getProductsByTagsFromElasticsearch(List<String> tags, int page, int limit, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return productSearchRepository.findByTagsInAndIsActiveTrue(tags, pageable)
                .map(doc -> convertToProductListResponse(doc, userId));
    }

    public Page<ProductListResponse> getSimilarProducts(Long productId, int page, int limit, Long userId) {
        ProductDocument productDoc = productSearchRepository.findById(ProductDocument.generateId(productId))
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (productDoc.getIsActive() == null || !productDoc.getIsActive()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), 
                    PageRequest.of(page - 1, limit), 0);
        }

        List<String> tags = productDoc.getTags() != null && !productDoc.getTags().isEmpty() 
                ? productDoc.getTags() 
                : List.of();
        Double price = productDoc.getPrice();
        String title = productDoc.getTitle() != null ? productDoc.getTitle() : "";
        String description = productDoc.getDescription() != null ? productDoc.getDescription() : "";
        String searchText = (title + " " + description).trim();

        Integer minPrice = price != null ? (int) (price * 0.7) : null;
        Integer maxPrice = price != null ? (int) (price * 1.3) : null;

        if (tags.isEmpty() && searchText.isBlank() && minPrice == null) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), 
                    PageRequest.of(page - 1, limit), 0);
        }

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ProductDocument> similarProducts = productSearchRepository.findSimilarProducts(
                productId,
                productDoc.getType(),
                tags,
                minPrice,
                maxPrice,
                searchText.isBlank() ? "*" : searchText,
                pageable
        );

        return similarProducts.map(doc -> convertToProductListResponse(doc, userId));
    }

    private ProductListResponse convertToProductListResponse(ProductDocument document) {
        return convertToProductListResponse(document, null);
    }

    private ProductListResponse convertToProductListResponse(ProductDocument document, Long userId) {
        String thumbnailUrl = document.getImageUrls() != null && !document.getImageUrls().isEmpty()
                ? document.getImageUrls().get(0) : null;

        Boolean isFavorite = userId != null && favoriteUsedItemRepository.existsByUserIdAndProductId(userId, document.getProductId());

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
                .isFavorite(isFavorite)
                .build();
    }

    private Sort createSort(String sortBy, String sortDir) {
        String field = sortBy != null ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
