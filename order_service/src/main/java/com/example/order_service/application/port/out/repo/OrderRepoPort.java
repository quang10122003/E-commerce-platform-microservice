package com.example.order_service.application.port.out.repo;

import com.example.order_service.application.dto.order.UserOrderItemView;
import com.example.order_service.application.dto.order.UserOrderView;
import com.example.order_service.application.dto.order.ShopOrderView;
import com.example.order_service.application.dto.order.ShopOrderSearch;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.example.common.response.PageResponse;
import com.example.common.response.PageQuery;

public interface OrderRepoPort {
    List<Long> saveAll(List<Order> orders);
    List<Order> findByCheckoutIdForUpdate(Long checkoutId);
    Optional<Order> findUserOrderForUpdate(Long orderId, Long userId);
    Optional<Order> findShopOrderForUpdate(Long orderId, Long shopId);
    List<Long> findShippingOrderIdsDueBefore(LocalDateTime cutoff, int limit);
    Optional<Order> findByIdForUpdate(Long orderId);
    void saveStatus(List<Order> orders);
    // keyword null là không lọc; orderId chỉ có khi keyword là mã đơn.
    PageResponse<UserOrderView> findUserOrders(Long userId, OrderStatus status, String keyword, Long orderId, PageQuery pageQuery);
    List<ShopOrderView> findShopOrders(ShopOrderSearch search);
    List<UserOrderItemView> findUserOrderItems(Collection<Long> orderIds);

    int clearDeletedProduct(Long productId);

    boolean existsActiveOrderOfUser(Long userId);

    int clearDeletedVariant(Long variantId);
}
