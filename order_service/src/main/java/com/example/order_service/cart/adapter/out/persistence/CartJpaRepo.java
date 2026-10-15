package com.example.order_service.cart.adapter.out.persistence;

import com.example.order_service.cart.adapter.entity.CartEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface CartJpaRepo extends JpaRepository<CartEntity,Long> {
    @EntityGraph(attributePaths = "items")
    Optional<CartEntity> findByUserId(Long userId);

    // Cập nhật giỏ bị ảnh hưởng trước khi xóa item, trong cùng transaction của application service.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update CartEntity c set c.updatedAt = :now
            where c.id in (select i.cart.id from CartItemEntity i where i.productVariantId in :variantIds)
            """)
    int updateCartsContainingVariants(@Param("variantIds") List<Long> variantIds,
                                     @Param("now") LocalDateTime now);

    // Bulk delete chỉ theo ID trong event, nhận lại cùng event sẽ trả về số dòng xóa bằng 0.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CartItemEntity i where i.productVariantId in :variantIds")
    int deleteItemsByVariantIds(@Param("variantIds") List<Long> variantIds);
}
