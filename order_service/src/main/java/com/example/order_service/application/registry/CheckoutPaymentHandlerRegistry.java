package com.example.order_service.application.registry;

import com.example.order_service.application.strategy.payment.CheckoutPaymentHandler;
import com.example.order_service.domain.model.CheckoutPaymentMethod;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CheckoutPaymentHandlerRegistry {

    private final Map<CheckoutPaymentMethod, CheckoutPaymentHandler> handlers;

    public CheckoutPaymentHandlerRegistry(List<CheckoutPaymentHandler> paymentHandlers) {
        this.handlers = paymentHandlers.stream().collect(Collectors.toUnmodifiableMap(
                CheckoutPaymentHandler::paymentMethod, Function.identity(),
                (CheckoutPaymentHandler first, CheckoutPaymentHandler second) -> {
                    throw new IllegalStateException("Trùng handler cho paymentMethod=" + first.paymentMethod());
                }));
    }

    public CheckoutPaymentHandler getHandler(CheckoutPaymentMethod paymentMethod) {
        return Optional.ofNullable(handlers.get(paymentMethod))
                .orElseThrow(() -> new IllegalStateException(
                        "Chưa đăng ký handler cho paymentMethod=" + paymentMethod));
    }
}
