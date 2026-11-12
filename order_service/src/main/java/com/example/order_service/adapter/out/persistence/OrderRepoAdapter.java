package com.example.order_service.adapter.out.persistence;

import com.example.order_service.adapter.entity.OrderEntity;
import com.example.order_service.adapter.entity.OrderItemEntity;
import com.example.order_service.adapter.mapper.OrderMapper;
import com.example.order_service.adapter.out.persistence.projection.UserOrderItemProjection;
import com.example.order_service.application.dto.order.UserOrderItemView;
import com.example.order_service.application.dto.order.UserOrderView;
import com.example.order_service.application.dto.order.ShopOrderView;
import com.example.order_service.application.dto.order.ShopOrderSearch;
import com.example.order_service.application.dto.order.ShopAnalyticsOrderView;
import com.example.order_service.application.dto.response.AdminAnalyticsResponse;
import com.example.order_service.application.dto.response.ShopAnalyticsResponse;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import com.example.order_service.application.port.out.repo.ShopAnalyticsQueryPort;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderItem;
import com.example.order_service.domain.model.OrderStatus;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepoAdapter implements OrderRepoPort, ShopAnalyticsQueryPort {
    private final OrderJpaRepo orders;
    private final OrderItemJpaRepo items;
    private final CheckoutJpaRepo checkouts;
    private final OrderMapper mapper;

    @Override
    public List<UserOrderView> findUserOrders(Long userId, OrderStatus status) {
        return orders.findUserOrders(userId, status).stream().map(mapper::toUserOrderView).toList();
    }

    @Override
    public List<ShopOrderView> findShopOrders(ShopOrderSearch search) {
        // Escape ký tự đặc biệt của LIKE bằng '!' giống query tìm kiếm của Product Service.
        String keyword = search.keyword() == null ? null
                : search.keyword().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return orders.findShopOrders(search.shopId(), search.status(), search.paymentMethod(), keyword,
                        search.orderId(), search.from(), search.to())
                .stream().map(mapper::toShopOrderView).toList();
    }

    @Override
    public List<ShopAnalyticsOrderView> findOrdersTouching(Long shopId, LocalDateTime from, LocalDateTime to) {
        return orders.findShopAnalyticsOrders(shopId, from, to).stream().map(mapper::toShopAnalyticsOrderView).toList();
    }

    @Override
    public List<ShopAnalyticsResponse.TopProduct> findTopProducts(Long shopId, LocalDateTime from, LocalDateTime to,
                                                                  int limit) {
        return items.findShopTopProducts(shopId, from, to, OrderStatus.CANCEL, PageRequest.of(0, limit)).stream()
                .map(mapper::toShopTopProduct).toList();
    }

    @Override
    public BigDecimal sumSaleDiscount(Long shopId, LocalDateTime from, LocalDateTime to) {
        return items.sumShopSaleDiscount(shopId, from, to, OrderStatus.CANCEL);
    }

    @Override
    public List<AdminAnalyticsResponse.TopShop> findTopShops(LocalDateTime from, LocalDateTime to, int limit) {
        return orders.findTopShops(from, to, OrderStatus.CANCEL, PageRequest.of(0, limit)).stream()
                .map(mapper::toTopShop).toList();
    }

    @Override
    public long countActiveShops(LocalDateTime from, LocalDateTime to) {
        return orders.countActiveShops(from, to);
    }

    @Override
    public List<UserOrderItemView> findUserOrderItems(Collection<Long> orderIds) {
        return items.findUserOrderItems(orderIds).stream()
                .map((UserOrderItemProjection item) -> new UserOrderItemView(item.getOrderId(),
                        mapper.toUserOrderItem(item)))
                .toList();
    }

    @Override
    public List<Long> saveAll(List<Order> values) {
        List<Long> ids = new ArrayList<>();
        for (Order value : values) {
            OrderEntity entity = mapper.toEntity(value);
            entity.setCheckout(checkouts.getReferenceById(value.getCheckoutId()));
            OrderEntity saved = orders.save(entity);
            List<OrderItemEntity> itemEntities = new ArrayList<>();
            for (OrderItem item : value.getItems()) {
                OrderItemEntity itemEntity = mapper.toEntity(item);
                itemEntity.setOrder(saved);
                itemEntities.add(itemEntity);
            }
            items.saveAll(itemEntities);
            ids.add(saved.getId());
        }
        return List.copyOf(ids);
    }

    @Override
    public List<Order> findByCheckoutIdForUpdate(Long checkoutId) {
        return orders.findByCheckoutIdForUpdate(checkoutId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Order> findUserOrderForUpdate(Long orderId, Long userId) {
        return orders.findUserOrderForUpdate(orderId, userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findShopOrderForUpdate(Long orderId, Long shopId) {
        return orders.findShopOrderForUpdate(orderId, shopId).map(mapper::toDomain);
    }

    @Override
    public List<Long> findShippingOrderIdsDueBefore(LocalDateTime cutoff, int limit) {
        return orders.findShippingOrderIdsDueBefore(cutoff, PageRequest.of(0, limit));
    }

    @Override
    public Optional<Order> findByIdForUpdate(Long orderId) {
        return orders.findByIdForUpdate(orderId).map(mapper::toDomain);
    }

    // Chỉ ghi các trường trạng thái; item của order không đổi nên không ghi lại.
    @Override
    public void saveStatus(List<Order> values) {
        for (Order value : values) {
            OrderEntity entity = orders.getReferenceById(value.getId());
            entity.setStatus(value.getStatus());
            entity.setCancelledBy(value.getCancelledBy());
            entity.setCancelReason(value.getCancelReason());
            entity.setCancelledAt(value.getCancelledAt());
            entity.setConfirmedAt(value.getConfirmedAt());
            entity.setShippedAt(value.getShippedAt());
            entity.setDeliveredAt(value.getDeliveredAt());
            entity.setUpdatedAt(value.getUpdatedAt());
        }
    }

    @Override
    public int clearDeletedProduct(Long productId) {
        return items.clearProductId(productId);
    }

    @Override
    public int clearDeletedVariant(Long variantId) {
        return items.clearProductVariantId(variantId);
    }

    @Override
    public boolean existsActiveOrderOfUser(Long userId) {
        return orders.existsByUserAndStatusIn(userId, java.util.List.of(
                com.example.order_service.domain.model.OrderStatus.PENDING_CONFIRMATION,
                com.example.order_service.domain.model.OrderStatus.PENDING_PICKUP,
                com.example.order_service.domain.model.OrderStatus.SHIPPING));
    }
}
