package io.github._3xhaust.root_server.domain.product.entity;

import io.github._3xhaust.root_server.domain.garagesale.entity.GarageSale;
import io.github._3xhaust.root_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private Short type; // 0 = USED, 1 = GARAGE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_sale_id")
    private GarageSale garageSale;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public Product(User seller, String title, Integer price, String description, String body, Short type, GarageSale garageSale) {
        this.seller = seller;
        this.title = title;
        this.price = price;
        this.description = description;
        this.body = body;
        this.type = type;
        this.garageSale = garageSale;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(String title, Integer price, String description, String body) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.body = body;
    }

    public void addImage(ProductImage productImage) {
        this.productImages.add(productImage);
    }

    public void clearImages() {
        this.productImages.clear();
    }
}

