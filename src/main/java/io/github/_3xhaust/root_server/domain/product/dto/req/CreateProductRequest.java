package io.github._3xhaust.root_server.domain.product.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "상품 제목은 필수입니다.")
    private String title;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    private String description;

    private String body;

    @NotNull(message = "상품 타입은 필수입니다.")
    private Short type; // 0 = USED, 1 = GARAGE

    private Long garageSaleId;

    private List<Long> imageIds;
}
