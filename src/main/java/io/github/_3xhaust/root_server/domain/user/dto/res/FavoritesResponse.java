package io.github._3xhaust.root_server.domain.user.dto.res;

import io.github._3xhaust.root_server.domain.garagesale.dto.res.GarageSaleListResponse;
import io.github._3xhaust.root_server.domain.product.dto.res.ProductListResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritesResponse {
    private List<ProductListResponse> products;
    private List<GarageSaleListResponse> garageSales;
}

