package io.github._3xhaust.root_server.domain.product.dto.res;

import io.github._3xhaust.root_server.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    private Long id;
    private String title;
    private Double price;
    private String description;
    private Short type;
    private String thumbnailUrl;
    private Instant createdAt;
    private SellerInfo seller;
    private Boolean isFavorite;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerInfo {
        private Long id;
        private String name;
    }

    public static ProductListResponse of(Product product) {
        return of(product, null);
    }

    public static ProductListResponse of(Product product, Boolean isFavorite) {
        String thumbnailUrl = null;
        if (!product.getProductImages().isEmpty()) {
            thumbnailUrl = product.getProductImages().get(0).getImage().getUrl();
        }

        return ProductListResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .price(product.getPrice())
                .description(product.getDescription())
                .type(product.getType())
                .thumbnailUrl(thumbnailUrl)
                .createdAt(product.getCreatedAt())
                .seller(SellerInfo.builder()
                        .id(product.getSeller().getId())
                        .name(product.getSeller().getName())
                        .build())
                .isFavorite(isFavorite)
                .build();
    }
}

