package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.OrderEntity;
import com.example.order_service.adapter.out.persistence.projection.UserOrderProjection;
import com.example.order_service.adapter.out.persistence.projection.ShopOrderProjection;
import com.example.order_service.adapter.out.persistence.projection.ShopAnalyticsOrderProjection;
import com.example.order_service.adapter.out.persistence.projection.TopShopProjection;
import com.example.order_service.adapter.out.persistence.projection.StatusCountProjection;
import com.example.order_service.adapter.out.persistence.projection.RecentOrderProjection;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.CheckoutPaymentMethod;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface OrderJpaRepo extends JpaRepository<OrderEntity, Long> {
    @Query("""
            select count(o) > 0 from OrderEntity o
            where o.status in :statuses and (o.shopId = :userId or o.checkout.userId = :userId)
            """)
    boolean existsByUserAndStatusIn(@Param("userId") Long userId,
                                    @Param("statuses") java.util.Collection<com.example.order_service.domain.model.OrderStatus> statuses);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.checkout.id = :checkoutId")
    List<OrderEntity> findByCheckoutIdForUpdate(@Param("checkoutId") Long checkoutId);

    // Lọc theo chủ checkout để user không khóa hoặc đọc được đơn của người khác.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :orderId and o.checkout.userId = :userId")
    Optional<OrderEntity> findUserOrderForUpdate(@Param("orderId") Long orderId, @Param("userId") Long userId);

    // Đơn đang giao đủ thời gian; đơn cũ chưa có shipped_at thì lấy updated_at (lúc chuyển sang SHIPPING) làm mốc.
    @Query("""
            select o.id from OrderEntity o
            where o.status = com.example.order_service.domain.model.OrderStatus.SHIPPING
              and coalesce(o.shippedAt, o.updatedAt) <= :cutoff
            order by o.id
            """)
    List<Long> findShippingOrderIdsDueBefore(@Param("cutoff") LocalDateTime cutoff, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :orderId")
    Optional<OrderEntity> findByIdForUpdate(@Param("orderId") Long orderId);

    // Lọc theo shop_id để shop không khóa hoặc thao tác được đơn của shop khác.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :orderId and o.shopId = :shopId")
    Optional<OrderEntity> findShopOrderForUpdate(@Param("orderId") Long orderId, @Param("shopId") Long shopId);

    @Query(value = """
            select o.id as orderId, c.id as checkoutId, o.status as status, o.shopId as shopId,
                   o.shopNameSnapshot as shopName, o.shopLogoUrlSnapshot as shopLogoUrl, o.priceType as priceType,
                   o.paymentMethod as paymentMethod, o.totalAmount as totalAmount, o.cancelledBy as cancelledBy, o.cancelReason as cancelReason,
                   o.cancelledAt as cancelledAt, o.confirmedAt as confirmedAt, o.shippedAt as shippedAt, o.deliveredAt as deliveredAt, o.createdAt as createdAt, p.status as paymentStatus,
                   p.paymentUrl as paymentUrl, p.expiresAt as paymentExpiresAt, p.paidAt as paymentPaidAt
            from OrderEntity o join o.checkout c left join PaymentEntity p on p.checkout = c
            where c.userId = :userId and (:status is null or o.status = :status)
              and (:keyword is null or o.id = :orderId
                   or lower(o.shopNameSnapshot) like lower(concat('%', :keyword, '%')) escape '!'
                   or exists (select 1 from OrderItemEntity i where i.order = o
                              and lower(i.productName) like lower(concat('%', :keyword, '%')) escape '!'))
            order by o.createdAt desc, o.id desc
            """, countQuery = """
            select count(o) from OrderEntity o join o.checkout c
            where c.userId = :userId and (:status is null or o.status = :status)
              and (:keyword is null or o.id = :orderId
                   or lower(o.shopNameSnapshot) like lower(concat('%', :keyword, '%')) escape '!'
                   or exists (select 1 from OrderItemEntity i where i.order = o
                              and lower(i.productName) like lower(concat('%', :keyword, '%')) escape '!'))
            """)
    Page<UserOrderProjection> findUserOrders(@Param("userId") Long userId, @Param("status") OrderStatus status,
                                             @Param("keyword") String keyword, @Param("orderId") Long orderId,
                                             Pageable pageable);

    // shop_id là userId của chủ shop; người mua lấy từ snapshot checkout, không gọi Auth Service.
    @Query(value = """
            select o.id as orderId, c.id as checkoutId, o.status as status, o.shopId as shopId,
                   o.shopNameSnapshot as shopName, o.shopLogoUrlSnapshot as shopLogoUrl, o.priceType as priceType,
                   o.paymentMethod as paymentMethod, o.totalAmount as totalAmount, o.cancelledBy as cancelledBy, o.cancelReason as cancelReason,
                   o.cancelledAt as cancelledAt, o.confirmedAt as confirmedAt, o.shippedAt as shippedAt, o.deliveredAt as deliveredAt, o.createdAt as createdAt, p.status as paymentStatus,
                   p.paymentUrl as paymentUrl, p.expiresAt as paymentExpiresAt, p.paidAt as paymentPaidAt,
                   c.recipientNameSnapshot as recipientName, c.buyerEmailSnapshot as buyerEmail,
                   c.shippingAddressSnapshot as shippingAddress, c.phoneSnapshot as phone, c.note as note
            from OrderEntity o join o.checkout c left join PaymentEntity p on p.checkout = c
            where o.shopId = :shopId
              and (:status is null or o.status = :status)
              and (:paymentMethod is null or o.paymentMethod = :paymentMethod)
              and (:from is null or o.createdAt >= :from)
              and (:to is null or o.createdAt <= :to)
              and (:keyword is null or o.id = :orderId
                   or lower(c.recipientNameSnapshot) like lower(concat('%', :keyword, '%')) escape '!'
                   or lower(c.buyerEmailSnapshot) like lower(concat('%', :keyword, '%')) escape '!'
                   or exists (select 1 from OrderItemEntity i where i.order = o
                              and lower(i.productName) like lower(concat('%', :keyword, '%')) escape '!'))
            order by o.createdAt desc, o.id desc
            """)
    List<ShopOrderProjection> findShopOrders(@Param("shopId") Long shopId, @Param("status") OrderStatus status,
                                             @Param("paymentMethod") CheckoutPaymentMethod paymentMethod,
                                             @Param("keyword") String keyword, @Param("orderId") Long orderId,
                                             @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select o.status as status, o.paymentMethod as paymentMethod, o.priceType as priceType,
                   o.totalAmount as totalAmount, o.cancelledBy as cancelledBy, o.cancelReason as cancelReason,
                   o.createdAt as createdAt, o.confirmedAt as confirmedAt, o.shippedAt as shippedAt,
                   o.deliveredAt as deliveredAt, o.cancelledAt as cancelledAt, p.paidAt as paidAt, c.userId as buyerId
            from OrderEntity o join o.checkout c left join PaymentEntity p on p.checkout = c
            where (:shopId is null or o.shopId = :shopId)
              and (o.createdAt between :from and :to or o.deliveredAt between :from and :to
                   or o.cancelledAt between :from and :to or p.paidAt between :from and :to)
            """)
    List<ShopAnalyticsOrderProjection> findShopAnalyticsOrders(@Param("shopId") Long shopId,
                                                               @Param("from") LocalDateTime from,
                                                               @Param("to") LocalDateTime to);

    @Query("""
            select o.shopId as shopId, max(o.shopNameSnapshot) as shopName, count(o) as orderCount,
                   sum(o.totalAmount) as revenue
            from OrderEntity o
            where o.createdAt between :from and :to and o.status <> :excluded
            group by o.shopId
            order by sum(o.totalAmount) desc
            """)
    List<TopShopProjection> findTopShops(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                         @Param("excluded") OrderStatus excluded, Pageable pageable);

    @Query("select count(distinct o.shopId) from OrderEntity o where o.createdAt between :from and :to")
    long countActiveShops(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            select o.status as status, count(o) as total from OrderEntity o
            where (:shopId is null or o.shopId = :shopId)
            group by o.status
            """)
    List<StatusCountProjection> countByStatus(@Param("shopId") Long shopId);

    @Query("""
            select o.id as orderId, o.shopId as shopId, o.shopNameSnapshot as shopName,
                   c.recipientNameSnapshot as recipientName, o.totalAmount as totalAmount, o.status as status,
                   o.paymentMethod as paymentMethod, o.createdAt as createdAt
            from OrderEntity o join o.checkout c
            where (:shopId is null or o.shopId = :shopId)
            order by o.createdAt desc, o.id desc
            """)
    List<RecentOrderProjection> findRecentOrders(@Param("shopId") Long shopId, Pageable pageable);
}
