package io.github._3xhaust.root_server.domain.product.dto.res;

import io.github._3xhaust.root_server.domain.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private SellerInfo seller;
    private String title;
    private Double price;
    private String description;
    private String body;
    private Short type;
    private Long garageSaleId;
    private List<String> imageUrls;
    private Boolean isFavorite;
    private Instant createdAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerInfo {
        private Long id;
        private String name;
        private String profileImageUrl;
        private Short rating;
    }

    public static ProductResponse of(Product product) {
        return of(product, null);
    }

    public static ProductResponse of(Product product, Boolean isFavorite) {
        return ProductResponse.builder()
                .id(product.getId())
                .seller(SellerInfo.builder()
                        .id(product.getSeller().getId())
                        .name(product.getSeller().getName())
                        .profileImageUrl(product.getSeller().getProfileImage() != null ?
                                product.getSeller().getProfileImage().getUrl() : null)
                        .rating(product.getSeller().getRating())
                        .build())
                .title(product.getTitle())
                .price(product.getPrice())
                .description(product.getDescription())
                .body(product.getBody())
                .type(product.getType())
                .garageSaleId(product.getGarageSale() != null ? product.getGarageSale().getId() : null)
                .imageUrls(product.getProductImages().stream()
                        .map(pi -> pi.getImage().getUrl())
                        .toList())
                .isFavorite(isFavorite)
                .createdAt(product.getCreatedAt())
                .build();
    }
}

