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

    // Phải cập nhật giỏ trước khi xóa item, trong cùng transaction để không mất dấu giỏ bị ảnh hưởng.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update CartEntity c set c.updatedAt = :now
            where c.id in (select i.cart.id from CartItemEntity i where i.productVariantId in :variantIds)
            """)
    int updateCartsContainingVariants(@Param("variantIds") List<Long> variantIds,
                                     @Param("now") LocalDateTime now);

    // Event nhận lại có thể xóa 0 dòng; đây là kết quả hợp lệ, không phải lỗi.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CartItemEntity i where i.productVariantId in :variantIds")
    int deleteItemsByVariantIds(@Param("variantIds") List<Long> variantIds);
}
