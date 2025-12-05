package io.github._3xhaust.root_server.domain.product.repository;

import io.github._3xhaust.root_server.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByType(Short type, Pageable pageable);

    Page<Product> findByGarageSaleId(Long garageSaleId, Pageable pageable);

    List<Product> findByGarageSaleId(Long garageSaleId);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId")
    Page<Product> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
}
