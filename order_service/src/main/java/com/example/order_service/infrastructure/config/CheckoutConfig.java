package com.example.order_service.infrastructure.config;

import com.example.order_service.application.port.out.client.ShopStatusPort;
import com.example.order_service.application.service.ShopAvailabilityService;

import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.client.ProductClientPort;
import com.example.order_service.application.service.CheckoutItemsService;
import com.example.order_service.application.service.CheckoutQuoteService;
import com.example.order_service.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.port.in.ProcessCheckoutEventUseCase;
import com.example.order_service.application.registry.KafkaEventHandlerRegistry;
import com.example.order_service.application.registry.CheckoutPaymentHandlerRegistry;
import com.example.order_service.application.port.out.repo.CheckoutRepoPort;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.application.service.CheckoutCreationService;
import com.example.order_service.application.service.CheckoutService;
import com.example.order_service.application.service.CheckoutInternalService;
import com.example.order_service.application.service.CheckoutEventApplicationService;
import com.example.order_service.application.service.CheckoutProcessingService;
import com.example.order_service.application.service.CheckoutCancellationService;
import com.example.order_service.application.strategy.eventKafka.CheckoutRequestedEventHandler;
import com.example.order_service.application.strategy.payment.CheckoutPaymentHandler;
import com.example.order_service.application.strategy.payment.CodCheckoutHandler;
import com.example.order_service.application.strategy.payment.VnpayCheckoutHandler;
import com.example.order_service.application.service.VnpayPaymentService;
import com.example.order_service.adapter.out.payment.VnpayPaymentAdapter;
import com.example.order_service.application.port.out.client.VnpayPaymentPort;
import com.example.order_service.application.port.out.client.VnpaySignaturePort;
import com.example.order_service.application.service.VnpayIpnService;
import com.example.order_service.application.port.in.ReconcileExpiredPaymentUseCase;
import com.example.order_service.application.port.in.AutoCompleteOrderUseCase;
import com.example.order_service.application.service.AutoCompleteOrderService;
import com.example.order_service.application.service.UserOrderService;
import com.example.order_service.application.service.ShopAnalyticsService;
import com.example.order_service.application.port.out.repo.ShopAnalyticsQueryPort;
import com.example.order_service.application.port.out.client.VnpayQueryPort;
import com.example.order_service.application.service.PaymentReconciliationService;
import com.example.order_service.application.service.PaymentOutcomeService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import com.example.order_service.application.strategy.outbox.CheckoutRequestedOutboxHandler;
import com.example.order_service.application.strategy.outbox.CheckoutInventoryRequestedOutboxHandler;
import com.example.order_service.application.strategy.outbox.CheckoutInventoryRollbackRequestedOutboxHandler;
import com.example.order_service.application.strategy.outbox.OrderInventoryRollbackRequestedOutboxHandler;
import com.example.order_service.application.strategy.outbox.OrderCancelledByShopOutboxHandler;
import com.example.order_service.application.strategy.outbox.OrderStatusChangedOutboxHandler;
import com.example.order_service.application.strategy.outbox.CheckoutSucceededOutboxHandler;
import com.example.order_service.application.strategy.outbox.CheckoutFailedOutboxHandler;
import com.example.order_service.application.strategy.eventKafka.CheckoutInventorySucceededHandler;
import com.example.order_service.application.strategy.eventKafka.CheckoutInventoryFailedHandler;
import com.example.order_service.application.strategy.eventKafka.CheckoutInventoryRolledBackHandler;
import com.example.order_service.application.port.out.client.ShopSnapshotPort;
import com.example.order_service.application.port.out.repo.InboxEventPort;
import com.example.order_service.application.port.out.repo.OrderRepoPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Value;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableConfigurationProperties(VnpayProperties.class)
public class CheckoutConfig {
    @Bean
    ShopAvailabilityService shopAvailabilityService(
            ProductClientPort productClientPort,
            ShopStatusPort shopStatusPort) {
        return new ShopAvailabilityService(productClientPort, shopStatusPort);
    }

    @Bean
    CheckoutQuoteService checkoutQuoteService(ProductClientPort productClientPort,
            ShopAvailabilityService shopAvailabilityService) {
        return new CheckoutQuoteService(productClientPort, shopAvailabilityService);
    }

    @Bean
    CheckoutItemsService checkoutItemsService(ProductClientPort productClientPort, Clock clock,
            ShopAvailabilityService shopAvailabilityService) {
        return new CheckoutItemsService(productClientPort, clock, shopAvailabilityService);
    }

    @Bean
    CheckoutInternalService checkoutInternalService(CurrentUserPort currentUserPort,
            CheckoutRepoPort checkoutRepoPort) {
        return new CheckoutInternalService(currentUserPort, checkoutRepoPort);
    }

    @Bean
    CheckoutCreationService checkoutCreationService(CheckoutRepoPort checkoutRepoPort,
            PaymentRepoPort paymentRepoPort, OutboxPort outboxPort, Clock clock) {
        return new CheckoutCreationService(checkoutRepoPort, paymentRepoPort, outboxPort, clock);
    }

    @Bean
    CheckoutService createCheckoutUseCase(CurrentUserPort currentUserPort,
            CheckoutRepoPort checkoutRepoPort, CheckoutCreationService checkoutCreationService,
            InboxEventPort inbox, OutboxPort outbox, OrderRepoPort orders,
            ShopSnapshotPort shops, ObjectMapperPort json, CheckoutCancellationService cancellation,
            PaymentRepoPort payments, VnpayPaymentService paymentUrls, Clock clock) {
        return new CheckoutService(currentUserPort, checkoutRepoPort, checkoutCreationService,
                inbox, outbox, orders, shops, json, cancellation, payments, paymentUrls, clock);
    }

    @Bean
    EventHandler<OutboxEventDto> checkoutRequestedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutRequestedOutboxHandler(publisher);
    }

    @Bean
    CheckoutPaymentHandler codCheckoutHandler(CheckoutService checkoutService) {
        return new CodCheckoutHandler(checkoutService);
    }

    @Bean
    CheckoutPaymentHandler vnpayCheckoutHandler(CheckoutService checkoutService) {
        return new VnpayCheckoutHandler(checkoutService);
    }

    @Bean
    VnpayPaymentAdapter vnpayPaymentAdapter(VnpayProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return new VnpayPaymentAdapter(properties, RestClient.builder().requestFactory(requestFactory).build());
    }

    // Thời gian chờ đọc từ cấu hình để môi trường test có thể rút ngắn.
    @Bean
    AutoCompleteOrderUseCase autoCompleteOrderUseCase(OrderRepoPort orderRepoPort, CheckoutRepoPort checkoutRepoPort,
            OutboxPort outboxPort, Clock clock,
            @Value("${app.order.auto-complete-after-hours:24}") long completeAfterHours) {
        return new AutoCompleteOrderService(orderRepoPort, checkoutRepoPort, outboxPort, clock,
                Duration.ofHours(completeAfterHours));
    }

    @Bean
    ReconcileExpiredPaymentUseCase reconcileExpiredPaymentUseCase(PaymentRepoPort paymentRepoPort,
            VnpayQueryPort vnpayQueryPort, Clock clock, VnpayProperties properties,
            PaymentOutcomeService paymentOutcomeService) {
        return new PaymentReconciliationService(paymentRepoPort, vnpayQueryPort, clock,
                properties.reconcileGraceMinutes(), paymentOutcomeService);
    }

    @Bean
    PaymentOutcomeService paymentOutcomeService(PaymentRepoPort paymentRepoPort,
            OrderRepoPort orderRepoPort, OutboxPort outboxPort, Clock clock) {
        return new PaymentOutcomeService(paymentRepoPort, orderRepoPort, outboxPort, clock);
    }

    @Bean
    VnpayPaymentService vnpayPaymentService(VnpayPaymentPort vnpayPaymentPort, PaymentRepoPort paymentRepoPort) {
        return new VnpayPaymentService(vnpayPaymentPort, paymentRepoPort);
    }

    @Bean
    ShopAnalyticsService shopAnalyticsService(CurrentUserPort currentUserPort,
                                              ShopAnalyticsQueryPort shopAnalyticsQueryPort, Clock clock) {
        return new ShopAnalyticsService(currentUserPort, shopAnalyticsQueryPort, clock);
    }

    @Bean
    UserOrderService userOrderService(CurrentUserPort currentUserPort, OrderRepoPort orderRepoPort,
                                      CheckoutRepoPort checkoutRepoPort, ShopAvailabilityService shopAvailabilityService,
                                      OutboxPort outboxPort, Clock clock) {
        return new UserOrderService(currentUserPort, orderRepoPort, checkoutRepoPort, shopAvailabilityService,
                outboxPort, clock);
    }

    @Bean
    VnpayIpnService vnpayIpnService(VnpaySignaturePort vnpaySignaturePort, PaymentRepoPort paymentRepoPort,
            Clock clock, PaymentOutcomeService paymentOutcomeService) {
        return new VnpayIpnService(vnpaySignaturePort, paymentRepoPort, clock, paymentOutcomeService);
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
    EventHandler<String> checkoutRequestedEventHandler(ObjectMapperPort objectMapperPort,
            CheckoutProcessingService checkoutProcessingService) {
        return new CheckoutRequestedEventHandler(objectMapperPort, checkoutProcessingService);
    }

    @Bean
    ProcessCheckoutEventUseCase processCheckoutEventUseCase(KafkaEventHandlerRegistry registry) {
        return new CheckoutEventApplicationService(registry);
    }

    @Bean
    EventHandler<String> checkoutInventorySucceededHandler(ObjectMapperPort json, CheckoutService service) {
        return new CheckoutInventorySucceededHandler(json, service);
    }

    @Bean
    EventHandler<String> checkoutInventoryFailedHandler(ObjectMapperPort json, CheckoutService service) {
        return new CheckoutInventoryFailedHandler(json, service);
    }

    @Bean
    EventHandler<String> checkoutInventoryRolledBackHandler(ObjectMapperPort json, CheckoutService service) {
        return new CheckoutInventoryRolledBackHandler(json, service);
    }

    @Bean
    EventHandler<OutboxEventDto> checkoutInventoryRequestedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutInventoryRequestedOutboxHandler(publisher);
    }

    @Bean
    EventHandler<OutboxEventDto> checkoutInventoryRollbackRequestedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutInventoryRollbackRequestedOutboxHandler(publisher);
    }

    // Phát yêu cầu hoàn tồn kho của order do user hủy sang Product.
    @Bean
    EventHandler<OutboxEventDto> orderInventoryRollbackRequestedOutboxHandler(OrderEventPublisherPort publisher) {
        return new OrderInventoryRollbackRequestedOutboxHandler(publisher);
    }

    // Phát event shop hủy đơn để Notification gửi mail cho người mua.
    @Bean
    EventHandler<OutboxEventDto> orderCancelledByShopOutboxHandler(OrderEventPublisherPort publisher) {
        return new OrderCancelledByShopOutboxHandler(publisher);
    }

    // Phát event shop chuyển trạng thái đơn để Notification gửi mail cho người mua.
    @Bean
    EventHandler<OutboxEventDto> orderStatusChangedOutboxHandler(OrderEventPublisherPort publisher) {
        return new OrderStatusChangedOutboxHandler(publisher);
    }

    @Bean
    EventHandler<OutboxEventDto> checkoutSucceededOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutSucceededOutboxHandler(publisher);
    }

    @Bean
    EventHandler<OutboxEventDto> checkoutFailedOutboxHandler(OrderEventPublisherPort publisher) {
        return new CheckoutFailedOutboxHandler(publisher);
    }

}
