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
    private Double price;

    @Column
    private Double sale;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    private Short type; // 0 = USED, 1 = GARAGE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_sale_id")
    private GarageSale garageSale;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImages = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public Product(User seller, String title, Double price, Double sale, String description, String body, Short type, GarageSale garageSale, Double latitude, Double longitude) {
        this.seller = seller;
        this.title = title;
        this.price = price;
        this.sale = sale;
        this.description = description;
        this.body = body;
        this.type = type;
        this.garageSale = garageSale;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(String title, Double price, Double sale, String description, String body, Double latitude, Double longitude) {
        this.title = title;
        this.price = price;
        this.sale = sale;
        this.description = description;
        this.body = body;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void addImage(ProductImage productImage) {
        this.productImages.add(productImage);
    }

    public void clearImages() {
        this.productImages.clear();
    }

    public void removeImage(ProductImage productImage) {
        if (productImage == null) return;

        boolean removed = this.productImages.remove(productImage);
        if (removed) return;

        final Long targetImageId = productImage.getImage() != null ? productImage.getImage().getId() : null;

        if (targetImageId != null) {
            this.productImages.removeIf(pi -> {
                if (pi == null || pi.getImage() == null) return false;
                final Long imgId = pi.getImage().getId();
                return imgId != null && imgId.equals(targetImageId);
            });
        } else {
            this.productImages.removeIf(pi -> pi.equals(productImage));
        }
    }
}
