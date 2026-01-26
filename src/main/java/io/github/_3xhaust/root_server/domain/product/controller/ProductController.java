package io.github._3xhaust.root_server.domain.product.controller;

import io.github._3xhaust.root_server.domain.history.service.HistoryService;
import io.github._3xhaust.root_server.domain.product.dto.req.CreateProductRequest;
import io.github._3xhaust.root_server.domain.product.dto.req.UpdateProductRequest;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductResponse;
import io.github._3xhaust.root_server.domain.product.service.ProductService;
import io.github._3xhaust.root_server.domain.user.service.UserService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Products", description = "Product management and search APIs for Australian marketplace")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final HistoryService historyService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<Page<ProductListResponse>> getProducts(
            Authentication authentication,
            @RequestParam(required = false) Short type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Long userId = authentication != null
                ? userService.getUserByName(((UserDetails) authentication.getPrincipal()).getUsername()).getId()
                : null;
        Page<ProductListResponse> products = productService.getProducts(type, page, limit, userId);
        return ApiResponse.ok(products);
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProductById(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        String userName = authentication != null
                ? ((UserDetails) authentication.getPrincipal()).getUsername()
                : null;
        historyService.recordView(userName, null, productId);
        Long userId = userName != null
                ? userService.getUserByName(userName).getId()
                : null;
        ProductResponse product = productService.getProductById(productId, userId);
        return ApiResponse.ok(product);
    }

    @Operation(
            summary = "Get similar products",
            description = "Returns similar products based on tags, price range (±30%), and title/description similarity. Uses Elasticsearch for intelligent matching optimized for Australian marketplace."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved similar products",
                    content = @Content(schema = @Schema(implementation = ProductListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Product not found"
            )
    })
    @GetMapping("/{productId}/similar")
    public ApiResponse<Page<ProductListResponse>> getSimilarProducts(
            Authentication authentication,
            @Parameter(description = "Product ID to find similar items for", example = "1", required = true) @PathVariable Long productId,
            @Parameter(description = "Page number (1-indexed)", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "12") @RequestParam(defaultValue = "12") int limit
    ) {
        Long userId = authentication != null
                ? userService.getUserByName(((UserDetails) authentication.getPrincipal()).getUsername()).getId()
                : null;
        Page<ProductListResponse> similarProducts = productService.getSimilarProducts(productId, page, limit, userId);
        return ApiResponse.ok(similarProducts);
    }

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(
            Authentication authentication,
            @RequestBody @Valid CreateProductRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ProductResponse product = productService.createProduct(userDetails.getUsername(), request);
        return ApiResponse.ok(product, "상품이 등록되었습니다.");
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestBody @Valid UpdateProductRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ProductResponse product = productService.updateProduct(userDetails.getUsername(), productId, request);
        return ApiResponse.ok(product, "상품이 수정되었습니다.");
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        productService.deleteProduct(userDetails.getUsername(), productId);
        return ApiResponse.ok((Void) null, "상품이 삭제되었습니다.");
    }

    @PostMapping("/{productId}/favorite")
    public ApiResponse<Void> toggleFavorite(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        productService.toggleFavorite(userDetails.getUsername(), productId);
        return ApiResponse.ok((Void) null, "관심 상품이 변경되었습니다.");
    }

    @GetMapping("/search")
    public ApiResponse<Page<ProductListResponse>> searchProducts(
            Authentication authentication,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Long userId = authentication != null
                ? userService.getUserByName(((UserDetails) authentication.getPrincipal()).getUsername()).getId()
                : null;
        Page<ProductListResponse> products = productService.searchProducts(title, minPrice, maxPrice, page, limit, sortBy, direction, userId);
        return ApiResponse.ok(products);
    }

    @PostMapping("/{productId}/images")
    public ApiResponse<Void> uploadProductImages(
            Authentication authentication,
            @PathVariable Long productId,
            @RequestParam List<Long> imageIds
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        productService.uploadProductImages(userDetails.getUsername(), productId, imageIds);
        return ApiResponse.ok((Void) null, "이미지가 업로드되었습니다.");
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ApiResponse<Void> deleteProductImage(
            Authentication authentication,
            @PathVariable Long productId,
            @PathVariable Long imageId
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        productService.deleteProductImage(userDetails.getUsername(), productId, imageId);
        return ApiResponse.ok((Void) null, "이미지가 삭제되었습니다.");
    }

    // Elasticsearch 기반 검색 엔드포인트
    @GetMapping("/used/search")
    public ApiResponse<Page<ProductListResponse>> searchUsedProducts(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {
        Long userId = authentication != null
                ? userService.getUserByName(((UserDetails) authentication.getPrincipal()).getUsername()).getId()
                : null;
        Page<ProductListResponse> products = productService.searchUsedProductsFromElasticsearch(keyword, page, limit, minPrice, maxPrice, userId);
        return ApiResponse.ok(products);
    }

    @GetMapping("/used")
    public ApiResponse<Page<ProductListResponse>> getUsedProducts(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Long userId = authentication != null
                ? userService.getUserByName(((UserDetails) authentication.getPrincipal()).getUsername()).getId()
                : null;
        Page<ProductListResponse> products = productService.getUsedProductsFromElasticsearch(page, limit, sortBy, sortDir, userId);
        return ApiResponse.ok(products);
    }

    @GetMapping("/tags")
    public ApiResponse<Page<ProductListResponse>> getProductsByTag(
            Authentication authentication,
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Long userId = authentication != null
                ? userService.getUserByName(((UserDetails) authentication.getPrincipal()).getUsername()).getId()
                : null;
        Page<ProductListResponse> products = productService.getProductsByTagFromElasticsearch(tag, page, limit, userId);
        return ApiResponse.ok(products);
    }

    @GetMapping("/tags/multiple")
    public ApiResponse<Page<ProductListResponse>> getProductsByTags(
            Authentication authentication,
            @RequestParam List<String> tags,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Long userId = authentication != null
                ? userService.getUserByName(((UserDetails) authentication.getPrincipal()).getUsername()).getId()
                : null;
        Page<ProductListResponse> products = productService.getProductsByTagsFromElasticsearch(tags, page, limit, userId);
        return ApiResponse.ok(products);
    }
}
