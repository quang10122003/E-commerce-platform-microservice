package com.example.order_service.infrastructure.config;

import com.example.order_service.cart.application.port.out.client.CurrentUserPort;
import com.example.order_service.cart.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.cart.application.port.out.outbox.OutboxPort;
import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.cart.application.strategy.outbox.OutboxEventHandler;
import com.example.order_service.order.application.port.in.ProcessCheckoutEventUseCase;
import com.example.order_service.order.application.registry.CheckoutEventHandlerRegistry;
import com.example.order_service.order.application.registry.CheckoutPaymentHandlerRegistry;
import com.example.order_service.order.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.order.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.order.application.service.CheckoutCreationService;
import com.example.order_service.order.application.service.CheckoutService;
import com.example.order_service.order.application.service.CheckoutEventApplicationService;
import com.example.order_service.order.application.service.CheckoutProcessingService;
import com.example.order_service.order.application.service.CheckoutCancellationService;
import com.example.order_service.order.application.strategy.eventKafka.EventHandler;
import com.example.order_service.order.application.strategy.eventKafka.CheckoutRequestedEventHandler;
import com.example.order_service.order.application.strategy.payment.CheckoutPaymentHandler;
import com.example.order_service.order.application.strategy.payment.CodCheckoutHandler;
import com.example.order_service.order.application.strategy.outbox.CheckoutRequestedOutboxHandler;
import com.example.order_service.order.application.strategy.outbox.CheckoutInventoryRequestedOutboxHandler;
import com.example.order_service.order.application.strategy.outbox.CheckoutInventoryRollbackRequestedOutboxHandler;
import com.example.order_service.order.application.strategy.outbox.CheckoutSucceededOutboxHandler;
import com.example.order_service.order.application.strategy.outbox.CheckoutFailedOutboxHandler;
import com.example.order_service.order.application.strategy.eventKafka.CheckoutInventorySucceededHandler;
import com.example.order_service.order.application.strategy.eventKafka.CheckoutInventoryFailedHandler;
import com.example.order_service.order.application.strategy.eventKafka.CheckoutInventoryRolledBackHandler;
import com.example.order_service.order.application.port.out.client.ShopSnapshotPort;
import com.example.order_service.order.application.port.out.repo.InboxEventPort;
import com.example.order_service.order.application.port.out.repo.OrderRepoPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class CheckoutConfig {

    @Bean
    CheckoutCreationService checkoutCreationService(CheckoutRepoPort checkoutRepoPort,
            PaymentRepoPort paymentRepoPort, OutboxPort outboxPort, Clock clock) {
        return new CheckoutCreationService(checkoutRepoPort, paymentRepoPort, outboxPort, clock);
    }

    @Bean
    CheckoutService createCheckoutUseCase(CurrentUserPort currentUserPort,
            CheckoutRepoPort checkoutRepoPort, CheckoutCreationService checkoutCreationService,
            InboxEventPort inbox, OutboxPort outbox, OrderRepoPort orders,
            ShopSnapshotPort shops, ObjectMapperPort json, CheckoutCancellationService cancellation, Clock clock) {
        return new CheckoutService(currentUserPort, checkoutRepoPort, checkoutCreationService,
                inbox, outbox, orders, shops, json, cancellation, clock);
    }

    @Bean
    OutboxEventHandler checkoutRequestedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutRequestedOutboxHandler(publisher);
    }

    @Bean
    CheckoutPaymentHandler codCheckoutHandler(CheckoutService checkoutService) {
        return new CodCheckoutHandler(checkoutService);
    }

    @Bean
    CheckoutPaymentHandlerRegistry checkoutPaymentHandlerRegistry(List<CheckoutPaymentHandler> handlers) {
        return new CheckoutPaymentHandlerRegistry(handlers);
    }

    @Bean
    CheckoutCancellationService checkoutCancellationService(CheckoutRepoPort checkoutRepoPort,
            PaymentRepoPort paymentRepoPort, Clock clock, OutboxPort outbox) {
        return new CheckoutCancellationService(checkoutRepoPort, paymentRepoPort, clock, outbox);
    }

    @Bean
    CheckoutProcessingService checkoutProcessingService(CheckoutPaymentHandlerRegistry registry,
            CheckoutRepoPort checkoutRepoPort, CheckoutCancellationService checkoutCancellationService,
            InboxEventPort inbox, ObjectMapperPort json, Clock clock) {
        return new CheckoutProcessingService(registry, checkoutRepoPort, checkoutCancellationService, inbox, json, clock);
    }

    @Bean
    EventHandler checkoutRequestedEventHandler(ObjectMapperPort objectMapperPort,
            CheckoutProcessingService checkoutProcessingService) {
        return new CheckoutRequestedEventHandler(objectMapperPort, checkoutProcessingService);
    }

    @Bean
    CheckoutEventHandlerRegistry checkoutEventHandlerRegistry(List<EventHandler> handlers) {
        return new CheckoutEventHandlerRegistry(handlers);
    }

    @Bean
    ProcessCheckoutEventUseCase processCheckoutEventUseCase(CheckoutEventHandlerRegistry registry) {
        return new CheckoutEventApplicationService(registry);
    }

    @Bean
    EventHandler checkoutInventorySucceededHandler(ObjectMapperPort json, CheckoutService service) {
        return new CheckoutInventorySucceededHandler(json, service);
    }

    @Bean
    EventHandler checkoutInventoryFailedHandler(ObjectMapperPort json, CheckoutService service) {
        return new CheckoutInventoryFailedHandler(json, service);
    }

    @Bean
    EventHandler checkoutInventoryRolledBackHandler(ObjectMapperPort json, CheckoutService service) {
        return new CheckoutInventoryRolledBackHandler(json, service);
    }

    @Bean
    OutboxEventHandler checkoutInventoryRequestedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutInventoryRequestedOutboxHandler(publisher);
    }

    @Bean
    OutboxEventHandler checkoutInventoryRollbackRequestedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutInventoryRollbackRequestedOutboxHandler(publisher);
    }

    @Bean
    OutboxEventHandler checkoutSucceededOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutSucceededOutboxHandler(publisher);
    }

    @Bean
    OutboxEventHandler checkoutFailedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutFailedOutboxHandler(publisher);
    }

}
