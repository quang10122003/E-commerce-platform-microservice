package com.example.notification.adapter.out.persistence.ChatConversation;

import com.example.notification.adapter.entity.ChatConversationsEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatConversationJpa extends JpaRepository<ChatConversationsEntity, Long> {

    // Ưu tiên room cập nhật mới nhất; ID phân xử khi trùng thời điểm cập nhật.
    @Query("""
            select conversation from ChatConversationsEntity conversation
            where conversation.customerId = :customerId
            order by conversation.updatedAt desc, conversation.id desc
            """)
    Page<ChatConversationsEntity> findConversationsByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    // Lọc đúng vai trò shop, không trộn các room tài khoản đã mở khi mua hàng.
    @Query("""
            select conversation from ChatConversationsEntity conversation
            where conversation.shopId = :shopId
            order by conversation.updatedAt desc, conversation.id desc
            """)
    Page<ChatConversationsEntity> findConversationsByShopId(@Param("shopId") Long shopId, Pageable pageable);

    // Các lần gửi cùng room phải giữ khóa đến khi lưu tin và metadata hoàn tất.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatConversationsEntity> findLockedById(Long id);

    // Unique key và upsert ngăn hai lần mở chat đồng thời tạo trùng room.
    @Modifying
    @Query(value = """
            INSERT INTO chat_conversations (customer_id, shop_id, product_id)
            VALUES (:customerId, :shopId, :productId)
            ON DUPLICATE KEY UPDATE id = id
            """, nativeQuery = true)
    void insertIfAbsent(@Param("customerId") Long customerId, @Param("shopId") Long shopId,
                        @Param("productId") Long productId);

    // Tra cứu room hiện có mà không khóa khoảng trống khi chưa có dữ liệu.
    Optional<ChatConversationsEntity> findByCustomerIdAndShopIdAndProductId(
            Long customerId, Long shopId, Long productId);

    // Đọc phiên bản hiện tại sau upsert, kể cả room vừa được transaction khác tạo.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ChatConversationsEntity> findLockedByCustomerIdAndShopIdAndProductId(
            Long customerId, Long shopId, Long productId);
}
