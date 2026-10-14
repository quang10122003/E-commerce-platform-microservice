package com.example.order_service.cart.adapter.in.rest;

import com.example.order_service.cart.application.dto.request.AddCartItemRequest;
import com.example.order_service.cart.application.dto.repone.CartRepone;
import com.example.order_service.cart.application.port.in.AddCartItemUseCase;
import com.example.order_service.cart.application.port.in.GetCartUseCase;
import com.example.order_service.cart.application.port.in.RemoveCartItemUseCase;
import com.example.order_service.cart.application.port.in.UpdateCartItemQuantityUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CartCotroller {

    GetCartUseCase getCartUseCase;
    AddCartItemUseCase addCartItemUseCase;
    UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    RemoveCartItemUseCase removeCartItemUseCase;

    @GetMapping
    public ResponseEntity<CartRepone> getCart(){
        return ResponseEntity.ok(getCartUseCase.getCart());
    }
    // Thêm biến thể với số lượng tùy chọn, mặc định thêm một sản phẩm.
    @PostMapping()
    public ResponseEntity<Void> addCart(@RequestParam Long productVariantId,
                                        @Valid @RequestBody(required = false) AddCartItemRequest request){
        addCartItemUseCase.addCartItem(productVariantId, request == null ? null : request.quantity());
        return ResponseEntity.status(201).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateCartItemQuantity(
            @RequestParam Long productVariantId,
            @RequestParam int quantity) {
        updateCartItemQuantityUseCase.updateCartItemQuantity(productVariantId, quantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeCartItem(@RequestParam Long productVariantId) {
        removeCartItemUseCase.removeCartItem(productVariantId);
        return ResponseEntity.noContent().build();
    }
}
