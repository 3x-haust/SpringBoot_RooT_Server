package io.github._3xhaust.root_server.domain.product.entity;

import java.io.Serializable;
import java.util.Objects;

public class ProductImageId implements Serializable {
    private Long product;
    private Long image;

    public ProductImageId() {}

    public ProductImageId(Long product, Long image) {
        this.product = product;
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductImageId that = (ProductImageId) o;
        return Objects.equals(product, that.product) && Objects.equals(image, that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, image);
    }
}

