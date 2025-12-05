package io.github._3xhaust.root_server.domain.product.entity;

import java.io.Serializable;
import java.util.Objects;

public class FavoriteUsedItemId implements Serializable {
    private Long user;
    private Long product;

    public FavoriteUsedItemId() {}

    public FavoriteUsedItemId(Long user, Long product) {
        this.user = user;
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteUsedItemId that = (FavoriteUsedItemId) o;
        return Objects.equals(user, that.user) && Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, product);
    }
}

