package com.example.order_service.infrastructure.config;

import com.example.order_service.cart.application.port.in.AddCartItemUseCase;
import com.example.order_service.cart.application.port.in.GetCartUseCase;
import com.example.order_service.cart.application.port.in.GetCountItemCartUseCase;
import com.example.order_service.cart.application.port.in.RemoveCartItemUseCase;
import com.example.order_service.cart.application.port.in.UpdateCartItemQuantityUseCase;
import com.example.order_service.cart.application.port.out.client.CurrentUserPort;
import com.example.order_service.cart.application.port.out.client.ProductClientPort;
import com.example.order_service.cart.application.port.out.repo.CartRepoPort;
import com.example.order_service.cart.application.service.CartService;
import com.example.order_service.cart.application.port.in.ProcessProductEventUseCase;
import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.cart.application.registry.KafkaEventHandlerRegistry;
import com.example.order_service.cart.application.service.ProductEventApplicationService;
import com.example.order_service.cart.application.strategy.eventKafka.EventHandler;
import com.example.order_service.cart.application.strategy.eventKafka.ProductDeleteEventHandler;
import com.example.order_service.cart.application.strategy.eventKafka.ProductvariantDeleteEventHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

@Configuration
public class CartConfig {
    // Cung cấp nguồn thời gian theo múi giờ Việt Nam.
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
    }

    // Khởi tạo application service từ các output port của luồng giỏ hàng.
    @Bean
    CartService cartService(
            CurrentUserPort currentUserPort,
            CartRepoPort cartRepoPort,
            ProductClientPort productClientPort,
            Clock clock) {
        return new CartService(currentUserPort, cartRepoPort, productClientPort, clock);
    }

    // Lắp ghép handler nội bộ với service giỏ hàng, không thêm vòng gọi qua input port.
    @Bean
    ProductDeleteEventHandler productDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService) {
        return new ProductDeleteEventHandler(objectMapperPort, cartService);
    }

    // Đăng ký strategy xóa một biến thể vào flow event product hiện có.
    @Bean
    ProductvariantDeleteEventHandler productvariantDeleteEventHandler(ObjectMapperPort objectMapperPort, CartService cartService) {
        return new ProductvariantDeleteEventHandler(objectMapperPort, cartService);
    }

    // Gom strategy theo eventType; registry kiểm tra key trùng khi khởi tạo.
    @Bean
    KafkaEventHandlerRegistry productEventHandlerRegistry(List<EventHandler> eventHandlers) {
        return new KafkaEventHandlerRegistry(eventHandlers);
    }

    // Chỉ công khai input port điều phối event cho consumer.
    @Bean
    ProcessProductEventUseCase processProductEventUseCase(KafkaEventHandlerRegistry eventHandlerRegistry) {
        return new ProductEventApplicationService(eventHandlerRegistry);
    }

    // Đăng ký use case giỏ hàng thông qua input port.
    @Bean
    GetCartUseCase getCartUseCase(CartService cartService) {
        return cartService;
    }

    // Đăng ký use case đếm item trong giỏ qua input port.
    @Bean
    GetCountItemCartUseCase getCountItemCartUseCase(CartService cartService) {
        return cartService;
    }

    // Đăng ký use case thêm sản phẩm qua input port.
    @Bean
    AddCartItemUseCase addCartItemUseCase(CartService cartService) {
        return cartService;
    }

    // Đăng ký use case cập nhật số lượng item qua input port.
    @Bean
    UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase(CartService cartService) {
        return cartService;
    }

    // Đăng ký use case xóa sản phẩm khỏi giỏ qua input port.
    @Bean
    RemoveCartItemUseCase removeCartItemUseCase(CartService cartService) {
        return cartService;
    }
}
