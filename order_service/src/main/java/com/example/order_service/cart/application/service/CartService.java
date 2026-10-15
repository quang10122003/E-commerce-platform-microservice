package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.repone.CartInfoRepone;
import com.example.order_service.cart.application.dto.repone.CartRepone;
import com.example.order_service.cart.application.dto.event.ProductDeleteEvent;
import com.example.order_service.cart.application.dto.event.ProductvariantDeleteEvent;
import com.example.order_service.cart.application.port.in.*;
import com.example.order_service.cart.application.port.out.client.CurrentUserPort;
import com.example.order_service.cart.application.port.out.client.ProductClientPort;
import com.example.order_service.cart.application.port.out.repo.CartRepoPort;
import com.example.order_service.cart.domain.model.Cart;
import com.example.order_service.cart.domain.model.CartItem;
import com.example.common.exception.BusinessException;
import com.example.common.untill.ValidationUtils;
import com.example.order_service.cart.domain.error.CartDomainError;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CartService implements GetCartUseCase, AddCartItemUseCase,
        UpdateCartItemQuantityUseCase, RemoveCartItemUseCase, GetCountItemCartUseCase {
    CurrentUserPort currentUserPort;
    CartRepoPort cartRepoPort;
    ProductClientPort productClientPort;
    Clock clock;

    // Lấy giỏ hàng và ghép thông tin sản phẩm, không thay đổi dữ liệu đã lưu.
    @Override
    @Transactional(readOnly = true)
    public CartRepone getCart() {
        Long userId = currentUserPort.getInfo().userId();
        log.debug("Bắt đầu lấy giỏ hàng: user_id={}", userId);
        Optional<Cart> cartOption = cartRepoPort.findByUserId(userId);
        // Trả giỏ rỗng khi người dùng chưa có giỏ hàng.
        if (cartOption.isEmpty()) {
            log.debug("Trả giỏ hàng rỗng: user_id={}", userId);
            return CartRepone.builder()
                    .CartId(null)
                    .totalItems(0)
                    .totalQuantity(0)
                    .item(List.of())
                    .build();
        }

        Cart cart = cartOption.get();
        // Lấy ID biến thể để truy vấn thông tin sản phẩm trong giỏ.
        List<Long> variantIds = cart.getItems().stream()
                .map(CartItem::getProductVariantId)
                .toList();
        // Lấy thông tin từ product service, bỏ qua truy vấn nếu giỏ rỗng.
        List<CartInfoRepone> productInfoList = variantIds.isEmpty()
                ? List.of()
                : productClientPort.getCartInfo(variantIds);

        Map<Long, CartInfoRepone> productInfoByVariantId = productInfoList.stream()
                .collect(Collectors.toMap(
                        CartInfoRepone::productVariantId,
                        productInfo -> productInfo,
                        (existing, duplicate) -> existing));

        // Chỉ ghép item có thông tin sản phẩm vào response, không xóa item khỏi giỏ.
        List<CartRepone.CartItemInfo> responseItems = cart.getItems().stream()
                .filter(cartItem -> productInfoByVariantId.containsKey(cartItem.getProductVariantId()))
                .map(cartItem -> toCartItemInfo(
                        cartItem,
                        productInfoByVariantId.get(cartItem.getProductVariantId())))
                .toList();

        log.debug("Đã lấy giỏ hàng: user_id={}, cart_id={}, total_items={}",
                userId, cart.getId(), responseItems.size());
        return CartRepone.builder()
                .CartId(cart.getId())
                .totalItems(responseItems.size())
                .totalQuantity(responseItems.stream().mapToInt(CartRepone.CartItemInfo::quantity).sum())
                .item(responseItems)
                .build();
    }

    // Ghép thông tin CartItemInfo của CartRepone
    private CartRepone.CartItemInfo toCartItemInfo(CartItem cartItem, CartInfoRepone productInfo) {
        List<CartRepone.VariantOptions> variantOptions = productInfo.variantOptions().stream()
                .map(option -> new CartRepone.VariantOptions(option.name(), option.value()))
                .toList();
        return CartRepone.CartItemInfo.builder()
                .cartItemId(cartItem.getId())
                .productId(productInfo.productId())
                .productVariantId(productInfo.productVariantId())
                .shopId(productInfo.shopId())
                .shopName(productInfo.shopName())
                .productName(productInfo.productName())
                .imageUrl(productInfo.imageUrl())
                .hasOption(productInfo.hasOption())
                .variantOptions(variantOptions)
                .price(productInfo.price())
                .quantity(cartItem.getQuantity())
                .stockQuantity(productInfo.stockQuantity())
                .status(productInfo.status())
                .sale(productInfo.sale() == null ? null
                        : new CartRepone.Sale(productInfo.sale().salePrice(), productInfo.sale().endsAt()))
                .build();
    }

    // Thêm một sản phẩm bằng luồng thêm giỏ với số lượng mặc định.
    @Override
    @Transactional
    public void  addCartItem(Long productVariantId) {
        addCartItem(productVariantId, null);
    }

    // Kiểm tra số lượng tùy chọn trước khi thêm biến thể vào giỏ.
    @Override
    @Transactional
    public void addCartItem(Long productVariantId, Integer quantity) {
        ValidationUtils.requirePositiveIfPresent(quantity, CartDomainError.QUANTITY_INVALID);
        Long userId = currentUserPort.getInfo().userId();
        log.debug("Bắt đầu thêm sản phẩm vào giỏ: user_id={}, product_variant_id={}, quantity={}",
                userId, productVariantId, quantity == null ? 1 : quantity);

        LocalDateTime now = LocalDateTime.now(clock);
        Cart cart;
        Optional<Cart> optionalCart = cartRepoPort.findByUserId(userId);
        // Tạo giỏ mới nếu người dùng chưa có giỏ hàng.
        cart = optionalCart.orElseGet(() -> Cart.create(userId, now));

        // Dùng overload mặc định của domain khi không truyền số lượng.
        if (quantity == null) {
            cart.addItem(productVariantId, now);
        } else {
            cart.addItem(productVariantId, quantity, now);
        }
        cartRepoPort.save(cart);
        log.info("Đã thêm sản phẩm vào giỏ: user_id={}, product_variant_id={}, added_quantity={}",
                userId, productVariantId, quantity == null ? 1 : quantity);
    }

    // Cập nhật số lượng biến thể trong giỏ của người dùng hiện tại.
    @Override
    @Transactional
    public void updateCartItemQuantity(Long productVariantId, int quantity) {
        Long userId = currentUserPort.getInfo().userId();
        log.debug("Bắt đầu cập nhật số lượng trong giỏ: user_id={}, product_variant_id={}, quantity={}",
                userId, productVariantId, quantity);
        Cart cart = cartRepoPort.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartDomainError.CART_NOT_FOUND));
        cart.updateItemQuantity(productVariantId, quantity, LocalDateTime.now(clock));
        cartRepoPort.save(cart);
        log.info("Đã cập nhật số lượng trong giỏ: user_id={}, product_variant_id={}, quantity={}",
                userId, productVariantId, quantity);
    }

    // Xóa biến thể khỏi giỏ của người dùng hiện tại.
    @Override
    @Transactional
    public void removeCartItem(Long productVariantId) {
        Long userId = currentUserPort.getInfo().userId();
        log.debug("Bắt đầu xóa sản phẩm khỏi giỏ: user_id={}, product_variant_id={}",
                userId, productVariantId);
        Cart cart = cartRepoPort.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartDomainError.CART_NOT_FOUND));
        cart.removeItem(productVariantId, LocalDateTime.now(clock));
        cartRepoPort.save(cart);
        log.info("Đã xóa sản phẩm khỏi giỏ: user_id={}, product_variant_id={}",
                userId, productVariantId);
    }

    // Dọn biến thể đã xóa trên mọi giỏ, không phụ thuộc người đăng nhập và an toàn khi nhận lại event.
    @Transactional
    public void removeDeletedProductVariants(ProductDeleteEvent event) {

        if (event == null || event.eventId() == null || event.productId() == null || event.productId() <= 0
                || !ProductDeleteEvent.EVENT_TYPE.equals(event.eventType()) || event.variantIds() == null
                || event.variantIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Event xóa sản phẩm không hợp lệ");
        }

        removeDeletedVariants(event.eventId(), event.eventType(), event.productId(), event.variantIds());
    }

    // Dọn một biến thể đã xóa trên mọi giỏ trong cùng transaction.
    @Transactional
    public void removeDeletedProductVariant(ProductvariantDeleteEvent event) {
        if (event == null || event.eventId() == null || event.productId() == null ||
                !ProductvariantDeleteEvent.EVENT_TYPE.equals(event.eventType())
                || event.variantId() == null ) {
            throw new IllegalArgumentException("Event xóa biến thể sản phẩm không hợp lệ");
        }
        removeDeletedVariants(event.eventId(), event.eventType(), event.productId(), List.of(event.variantId()));
    }

    // Dùng chung thao tác dọn giỏ cho event xóa sản phẩm và xóa một biến thể.
    private void removeDeletedVariants(UUID eventId, String eventType, Long productId, List<Long> deletedVariantIds) {
        List<Long> variantIds = deletedVariantIds.stream().distinct().toList();

        if (variantIds.isEmpty()) {
            log.info("Không có biến thể cần dọn giỏ: event_id={}, event_type={}, product_id={}",
                    eventId, eventType, productId);
            return;
        }

        log.debug("Bắt đầu dọn giỏ theo event: event_id={}, event_type={}, product_id={}, variant_count={}",
                eventId, eventType, productId, variantIds.size());

        int removedItems = cartRepoPort.removeItemsByVariantIds(variantIds, LocalDateTime.now(clock));

        log.info("Dọn giỏ theo event thành công: event_id={}, event_type={}, product_id={}, variant_count={}, removed_items={}",
                eventId, eventType, productId, variantIds.size(), removedItems);
    }

    // Đếm số dòng biến thể trong giỏ, trả 0 khi chưa có giỏ hàng.
    @Override
    @Transactional(readOnly = true)
    public int getCountItemCart() {
        Long userId = currentUserPort.getInfo().userId();
        int count = cartRepoPort.findByUserId(userId).map((cart) -> cart.getItems().size())
                .orElse(0);
        log.debug("Đã đếm item trong giỏ: user_id={}, total_items={}", userId, count);
        return count;
    }
}
