package io.github._3xhaust.root_server.domain.product.dto.req;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String title;

    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    private String description;

    private String body;

    private List<Long> imageIds;
}

