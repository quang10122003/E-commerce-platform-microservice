package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.repone.CartInfoRepone;
import com.example.order_service.cart.application.dto.repone.CartRepone;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    // Lấy giỏ hàng và loại bỏ biến thể không còn thông tin sản phẩm.
    @Override
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

        Set<Long> returnedVariantIds = productInfoByVariantId.keySet();

        // Xác định biến thể không còn được product service trả về.
        List<Long> missingVariantIds = cart.getItems().stream()
                .map(CartItem::getProductVariantId)
                .filter(variantId -> !returnedVariantIds.contains(variantId))
                .toList();
        // Xóa biến thể khỏi aggregate trước khi lưu thay đổi.
        LocalDateTime now = LocalDateTime.now(clock);
        missingVariantIds.forEach(variantId -> cart.removeItem(variantId, now));

        // Chỉ lưu giỏ khi có biến thể bị loại bỏ.
        if (!missingVariantIds.isEmpty()) {
            cartRepoPort.save(cart);
            log.info("Đã loại bỏ biến thể thiếu thông tin: user_id={}, cart_id={}, removed_items={}",
                    userId, cart.getId(), missingVariantIds.size());
        }

        List<CartRepone.CartItemInfo> responseItems = cart.getItems().stream()
                .map(cartItem -> toCartItemInfo(
                        cartItem,
                        productInfoByVariantId.get(cartItem.getProductVariantId())))
                .toList();

        log.debug("Đã lấy giỏ hàng: user_id={}, cart_id={}, total_items={}",
                userId, cart.getId(), responseItems.size());
        return CartRepone.builder()
                .CartId(cart.getId())
                .totalItems(responseItems.size())
                .totalQuantity(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum())
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
    public void  addCartItem(Long productVariantId) {
        addCartItem(productVariantId, null);
    }

    // Kiểm tra số lượng tùy chọn trước khi thêm biến thể vào giỏ.
    @Override
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

    // Đếm số dòng biến thể trong giỏ, trả 0 khi chưa có giỏ hàng.
    @Override
    public int getCountItemCart() {
        Long userId = currentUserPort.getInfo().userId();
        int count = cartRepoPort.findByUserId(userId).map((cart) -> cart.getItems().size())
                .orElse(0);
        log.debug("Đã đếm item trong giỏ: user_id={}, total_items={}", userId, count);
        return count;
    }
}
