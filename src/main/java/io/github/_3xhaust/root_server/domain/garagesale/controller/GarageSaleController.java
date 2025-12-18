package io.github._3xhaust.root_server.domain.garagesale.controller;

import io.github._3xhaust.root_server.domain.garagesale.dto.req.CreateGarageSaleRequest;
import io.github._3xhaust.root_server.domain.garagesale.dto.req.UpdateGarageSaleRequest;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleResponse;
import io.github._3xhaust.root_server.domain.garagesale.service.GarageSaleService;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductResponse;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/garage-sales")
@RequiredArgsConstructor
public class GarageSaleController {

    private final GarageSaleService garageSaleService;

    @GetMapping
    public ApiResponse<List<GarageSaleListResponse>> getAllGarageSales(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "10") Double radius
    ) {
        List<GarageSaleListResponse> garageSales = garageSaleService.getAllGarageSales(lat, lng, radius);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/{id}")
    public ApiResponse<GarageSaleResponse> getGarageSaleById(@PathVariable Long id) {
        GarageSaleResponse garageSale = garageSaleService.getGarageSaleById(id);
        return ApiResponse.ok(garageSale);
    }

    @PostMapping
    public ApiResponse<GarageSaleResponse> createGarageSale(
            Authentication authentication,
            @RequestBody @Valid CreateGarageSaleRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        GarageSaleResponse garageSale = garageSaleService.createGarageSale(userDetails.getUsername(), request);
        return ApiResponse.ok(garageSale, "개러지 세일이 등록되었습니다.");
    }

    @PutMapping("/{id}")
    public ApiResponse<GarageSaleResponse> updateGarageSale(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody @Valid UpdateGarageSaleRequest request
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        GarageSaleResponse garageSale = garageSaleService.updateGarageSale(userDetails.getUsername(), id, request);
        return ApiResponse.ok(garageSale, "개러지 세일이 수정되었습니다.");
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> toggleFavoriteGarageSale(
            Authentication authentication,
            @PathVariable Long id
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        garageSaleService.toggleFavoriteGarageSale(userDetails.getUsername(), id);
        return ApiResponse.ok((Void) null, "즐겨찾기가 변경되었습니다.");
    }

    @GetMapping("/{id}/{productId}")
    public ApiResponse<ProductResponse> getGarageSaleProduct(
            @PathVariable Long id,
            @PathVariable Long productId
    ) {
        ProductResponse product = garageSaleService.getGarageSaleProduct(id, productId);
        return ApiResponse.ok(product);
    }

    @PostMapping("/{id}/{productId}/favorite")
    public ApiResponse<Void> toggleFavoriteGarageSaleProduct(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Long productId
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        garageSaleService.toggleFavoriteGarageSaleProduct(userDetails.getUsername(), id, productId);
        return ApiResponse.ok((Void) null, "관심 상품이 변경되었습니다.");
    }
}

