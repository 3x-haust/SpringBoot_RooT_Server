package io.github._3xhaust.root_server.domain.search.controller;

import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.garagesale.service.GarageSaleService;
import io.github._3xhaust.root_server.domain.history.service.HistoryService;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import io.github._3xhaust.root_server.domain.product.service.ProductService;
import io.github._3xhaust.root_server.domain.search.dto.res.SearchResponse;
import io.github._3xhaust.root_server.domain.user.service.UserService;
import io.github._3xhaust.root_server.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;
    private final GarageSaleService garageSaleService;
    private final HistoryService historyService;
    private final UserService userService;

    @GetMapping
    public ApiResponse<SearchResponse> search(
            Authentication authentication,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        String userName = authentication != null
                ? ((UserDetails) authentication.getPrincipal()).getUsername()
                : null;
        historyService.recordSearch(userName, keyword, null, null);

        Long userId = userName != null
                ? userService.getUserByName(userName).getId()
                : null;
        Page<ProductListResponse> productPage = productService.searchUsedProductsFromElasticsearch(keyword, page, limit, null, null, userId);
        Page<GarageSaleListResponse> garageSalePage = garageSaleService.searchGarageSalesByKeyword(keyword, page, limit);

        SearchResponse response = SearchResponse.builder()
                .keyword(keyword)
                .products(productPage.getContent())
                .garageSales(garageSalePage.getContent())
                .pageInfo(SearchResponse.SearchPageInfo.builder()
                        .productPage(page)
                        .productTotalPages(productPage.getTotalPages())
                        .productTotalElements(productPage.getTotalElements())
                        .garageSalePage(page)
                        .garageSaleTotalPages(garageSalePage.getTotalPages())
                        .garageSaleTotalElements(garageSalePage.getTotalElements())
                        .build())
                .build();

        return ApiResponse.ok(response);
    }
}

