package io.github._3xhaust.root_server.domain.product.controller;

import io.github._3xhaust.root_server.domain.product.dto.req.CreateProductRequest;
import io.github._3xhaust.root_server.domain.product.dto.req.UpdateProductRequest;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductResponse;
import io.github._3xhaust.root_server.domain.product.service.ProductService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<Page<ProductListResponse>> getProducts(
            @RequestParam(required = false) Short type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<ProductListResponse> products = productService.getProducts(type, page, limit);
        return ApiResponse.ok(products);
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable Long productId) {
        ProductResponse product = productService.getProductById(productId);
        return ApiResponse.ok(product);
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
        return ApiResponse.ok(null, "상품이 삭제되었습니다.");
    }

    @PostMapping("/{productId}/favorite")
    public ApiResponse<Void> toggleFavorite(
            Authentication authentication,
            @PathVariable Long productId
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        productService.toggleFavorite(userDetails.getUsername(), productId);
        return ApiResponse.ok(null, "관심 상품이 변경되었습니다.");
    }
}
