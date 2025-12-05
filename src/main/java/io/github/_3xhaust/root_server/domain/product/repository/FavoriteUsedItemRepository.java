package io.github._3xhaust.root_server.domain.product.repository;

import io.github._3xhaust.root_server.domain.product.entity.FavoriteUsedItem;
import io.github._3xhaust.root_server.domain.product.entity.FavoriteUsedItemId;
import io.github._3xhaust.root_server.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteUsedItemRepository extends JpaRepository<FavoriteUsedItem, FavoriteUsedItemId> {
    @Query("SELECT f.product FROM FavoriteUsedItem f WHERE f.user.id = :userId")
    List<Product> findProductsByUserId(@Param("userId") Long userId);

    Optional<FavoriteUsedItem> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);
}

