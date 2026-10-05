package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.dto.repone.CartInfoRepone;
import com.example.order_service.cart.application.dto.repone.CartRepone;
import com.example.order_service.cart.application.port.in.AddCartItemUseCase;
import com.example.order_service.cart.application.port.in.GetCartUseCase;
import com.example.order_service.cart.application.port.in.RemoveCartItemUseCase;
import com.example.order_service.cart.application.port.in.UpdateCartItemQuantityUseCase;
import com.example.order_service.cart.application.port.out.client.CurrentUserPort;
import com.example.order_service.cart.application.port.out.client.ProductClientPort;
import com.example.order_service.cart.application.port.out.repo.CartRepoPort;
import com.example.order_service.cart.domain.model.Cart;
import com.example.order_service.cart.domain.model.CartItem;
import com.example.common.exception.BusinessException;
import com.example.order_service.cart.domain.error.CartDomainError;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CartService implements GetCartUseCase, AddCartItemUseCase,
        UpdateCartItemQuantityUseCase, RemoveCartItemUseCase {
    CurrentUserPort currentUserPort;
    CartRepoPort cartRepoPort;
    ProductClientPort productClientPort;

    // lấy giở hàng
    @Override
    public CartRepone getCart() {
        Long userId = currentUserPort.getInfo().userId();
        Optional<Cart> cartOption = cartRepoPort.findByUserId(userId);
//        ko tìm thấy giở thì trả giỏ rỗng
        if (cartOption.isEmpty()) {
            return CartRepone.builder()
                    .CartId(null)
                    .totalItems(0)
                    .totalQuantity(0)
                    .item(List.of())
                    .build();
        }

        Cart cart = cartOption.get();
        // lấy id của các sản phẩm trong giỏ
        List<Long> variantIds = cart.getItems().stream()
                .map(CartItem::getProductVariantId)
                .toList();
        // thông tin của các sản phẩm lấy từ product service
        List<CartInfoRepone> productInfoList = variantIds.isEmpty()
                ? List.of()
                : productClientPort.getCartInfo(variantIds);

        Map<Long, CartInfoRepone> productInfoByVariantId = productInfoList.stream()
                .collect(Collectors.toMap(
                        CartInfoRepone::productVariantId,
                        productInfo -> productInfo,
                        (existing, duplicate) -> existing));

        Set<Long> returnedVariantIds = productInfoByVariantId.keySet();

        // lấy các sản phẩm trong giỏ mà k có thông tin ( có thể product service đã xóa sản phẩm )
        List<Long> missingVariantIds = cart.getItems().stream()
                .map(CartItem::getProductVariantId)
                .filter(variantId -> !returnedVariantIds.contains(variantId))
                .toList();
        // gọi method xóa sản phẩm khỏi giỏ ( hành đôngj doamin ) chưa truy vấn
        missingVariantIds.forEach(cart::removeItem);

        // nếu có sản phẩm bị xóa thì gọi đê cập nhật giỏ trong db
        if (!missingVariantIds.isEmpty()) {
            cartRepoPort.save(cart);
        }

        List<CartRepone.CartItemInfo> responseItems = cart.getItems().stream()
                .map(cartItem -> toCartItemInfo(
                        cartItem,
                        productInfoByVariantId.get(cartItem.getProductVariantId())))
                .toList();

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
                .build();
    }

    // thêm sản phâm vào giỏ
    @Override
    public void  addCartItem(Long productVariantId) {
        Long userId = currentUserPort.getInfo().userId();

        Cart cart;
        Optional<Cart> optionalCart = cartRepoPort.findByUserId(userId);
        // k giỏ thì tạo và thêm sản phẩm
        if (optionalCart.isEmpty()) {
            cart = Cart.create(userId);
            cart.addItem(productVariantId);
        } else {
            cart = optionalCart.get();
            cart.addItem(productVariantId);
        }
        cartRepoPort.save(cart);
    }

    // Cập nhật số lượng biến thể trong giỏ của người dùng hiện tại.
    @Override
    public void updateCartItemQuantity(Long productVariantId, int quantity) {
        Long userId = currentUserPort.getInfo().userId();
        Cart cart = cartRepoPort.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartDomainError.ITEM_NOT_FOUND));
        cart.updateItemQuantity(productVariantId, quantity);
        cartRepoPort.save(cart);
    }

    // Xóa biến thể khỏi giỏ của người dùng hiện tại.
    @Override
    public void removeCartItem(Long productVariantId) {
        Long userId = currentUserPort.getInfo().userId();
        Cart cart = cartRepoPort.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(CartDomainError.ITEM_NOT_FOUND));
        cart.removeItem(productVariantId);
        cartRepoPort.save(cart);
    }
}
