package io.github._3xhaust.root_server.domain.product.entity;

import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_used_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(FavoriteUsedItemId.class)
public class FavoriteUsedItem {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Builder
    public FavoriteUsedItem(User user, Product product) {
        this.user = user;
        this.product = product;
    }
}

