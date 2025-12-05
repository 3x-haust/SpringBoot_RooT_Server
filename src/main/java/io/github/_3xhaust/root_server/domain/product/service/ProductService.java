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

    public Page<ProductListResponse> getProducts(Short type, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> products;
        if (type != null) {
            products = productRepository.findByType(type, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(ProductListResponse::of);
    }

    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));
        return ProductResponse.of(product);
    }

    @Transactional
    public ProductResponse createProduct(String email, CreateProductRequest request) {
        User seller = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

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

        return ProductResponse.of(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(String email, Long productId, UpdateProductRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new ProductException(ProductErrorCode.UNAUTHORIZED_ACCESS, "productId=" + productId);
        }

        product.update(
                request.getTitle() != null ? request.getTitle() : product.getTitle(),
                request.getPrice() != null ? request.getPrice() : product.getPrice(),
                request.getDescription() != null ? request.getDescription() : product.getDescription(),
                request.getBody() != null ? request.getBody() : product.getBody()
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

        return ProductResponse.of(product);
    }

    @Transactional
    public void deleteProduct(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND, "id=" + productId));

        if (!product.getSeller().getId().equals(user.getId())) {
            throw new ProductException(ProductErrorCode.UNAUTHORIZED_ACCESS, "productId=" + productId);
        }

        productRepository.delete(product);
    }

    @Transactional
    public void toggleFavorite(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

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

    public List<ProductListResponse> getFavoriteProducts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "email=" + email));

        List<Product> products = favoriteUsedItemRepository.findProductsByUserId(user.getId());
        return products.stream()
                .map(ProductListResponse::of)
                .toList();
    }
}

