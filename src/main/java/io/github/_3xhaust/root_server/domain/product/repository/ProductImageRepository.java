package io.github._3xhaust.root_server.domain.product.repository;

import io.github._3xhaust.root_server.domain.product.entity.ProductImage;
import io.github._3xhaust.root_server.domain.product.entity.ProductImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, ProductImageId> {
    List<ProductImage> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}

