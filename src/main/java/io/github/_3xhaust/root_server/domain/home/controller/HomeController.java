package io.github._3xhaust.root_server.domain.home.controller;

import io.github._3xhaust.root_server.domain.home.dto.res.*;
import io.github._3xhaust.root_server.domain.home.facade.HomeFacade;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeFacade homeFacade;

    @GetMapping("/feed")
    public ApiResponse<HomeFeedResponse> getHomeFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "5") Double radiusKm
    ) {
        HomeFeedResponse response = homeFacade.getHomeFeed(page, limit, latitude, longitude, radiusKm);
        return ApiResponse.ok(response);
    }

    @GetMapping("/products/used")
    public ApiResponse<Page<HomeProductResponse>> getUsedProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<HomeProductResponse> products = homeFacade.getUsedProducts(page, limit, sortBy, sortDir);
        return ApiResponse.ok(products);
    }

    @GetMapping("/products/used/search")
    public ApiResponse<Page<HomeProductResponse>> searchUsedProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice
    ) {
        Page<HomeProductResponse> products = homeFacade.searchUsedProducts(keyword, page, limit, minPrice, maxPrice);
        return ApiResponse.ok(products);
    }

    @GetMapping("/garage-sales")
    public ApiResponse<Page<HomeGarageSaleResponse>> getGarageSales(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.getGarageSales(page, limit, sortBy, sortDir);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/garage-sales/{garageSaleId}")
    public ApiResponse<GarageSaleDetailResponse> getGarageSaleDetail(
            @PathVariable Long garageSaleId,
            @RequestParam(defaultValue = "1") int productPage,
            @RequestParam(defaultValue = "20") int productLimit
    ) {
        GarageSaleDetailResponse response = homeFacade.getGarageSaleDetail(garageSaleId, productPage, productLimit);
        return ApiResponse.ok(response);
    }

    @GetMapping("/garage-sales/nearby")
    public ApiResponse<Page<HomeGarageSaleResponse>> getNearbyGarageSales(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Double radiusKm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.searchNearbyGarageSales(
                latitude, longitude, radiusKm, page, limit);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/garage-sales/current")
    public ApiResponse<Page<HomeGarageSaleResponse>> getCurrentGarageSales(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.getCurrentGarageSales(page, limit);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/garage-sales/schedule")
    public ApiResponse<Page<HomeGarageSaleResponse>> getGarageSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.getGarageSalesByDateRange(
                startDate, endDate, page, limit);
        return ApiResponse.ok(garageSales);
    }

    @GetMapping("/search")
    public ApiResponse<SearchResponse> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        SearchResponse response = homeFacade.search(keyword, page, limit);
        return ApiResponse.ok(response);
    }

    @GetMapping("/products/tags")
    public ApiResponse<Page<HomeProductResponse>> getProductsByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeProductResponse> products = homeFacade.getProductsByTag(tag, page, limit);
        return ApiResponse.ok(products);
    }

    @GetMapping("/products/tags/multiple")
    public ApiResponse<Page<HomeProductResponse>> getProductsByTags(
            @RequestParam List<String> tags,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeProductResponse> products = homeFacade.getProductsByTags(tags, page, limit);
        return ApiResponse.ok(products);
    }

    @GetMapping("/garage-sales/tags")
    public ApiResponse<Page<HomeGarageSaleResponse>> getGarageSalesByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Page<HomeGarageSaleResponse> garageSales = homeFacade.getGarageSalesByTag(tag, page, limit);
        return ApiResponse.ok(garageSales);
    }
}

