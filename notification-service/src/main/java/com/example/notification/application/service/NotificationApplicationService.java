package com.example.notification.application.service;

import com.example.notification.application.dto.request.EmailMessage;
import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.dto.event.CheckoutSucceededNotification;
import com.example.notification.application.dto.template.CheckoutSucceededEmailTemplate;
import com.example.notification.application.dto.template.OrderCancelledByShopEmailTemplate;
import com.example.notification.application.dto.template.OrderStatusChangedEmailTemplate;
import com.example.notification.application.dto.event.OrderStatusChangedNotification;
import com.example.notification.application.dto.event.OrderCancelledByShopNotification;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;
import com.example.notification.application.port.in.SendEmailUserRegisteredUseCase;
import com.example.notification.application.port.out.mail.EmailSenderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class NotificationApplicationService
        implements SendEmailUserRegisteredUseCase {

    private final EmailSenderPort emailSenderPort;
    private final UserRegisteredEmailTemplate userRegisteredEmailTemplate;
    private final CheckoutSucceededEmailTemplate checkoutSucceededEmailTemplate;
    private final OrderCancelledByShopEmailTemplate orderCancelledByShopEmailTemplate;
    private final OrderStatusChangedEmailTemplate orderStatusChangedEmailTemplate;

    public void sendCheckoutSucceededEmail(CheckoutSucceededNotification event) {
        log.info("Chuẩn bị gửi email đặt hàng thành công: event_id={}, checkout_id={}",
                event.eventId(), event.checkoutId());
        EmailMessage message = checkoutSucceededEmailTemplate.build(event);
        emailSenderPort.send(message);
        log.info("Gửi email đặt hàng thành công: event_id={}, checkout_id={}",
                event.eventId(), event.checkoutId());
    }

    // Báo người mua đơn bị shop hủy; lỗi gửi mail được ném ra để Kafka thử lại.
    public void sendOrderCancelledByShopEmail(OrderCancelledByShopNotification event) {
        log.info("Chuẩn bị gửi email shop hủy đơn: event_id={}, order_id={}", event.eventId(), event.orderId());
        emailSenderPort.send(orderCancelledByShopEmailTemplate.build(event));
        log.info("Gửi email shop hủy đơn thành công: event_id={}, order_id={}", event.eventId(), event.orderId());
    }

    // Báo người mua đơn đã được xác nhận hoặc đang giao; lỗi gửi mail được ném ra để Kafka thử lại.
    public void sendOrderStatusChangedEmail(OrderStatusChangedNotification event) {
        log.info("Chuẩn bị gửi email đổi trạng thái đơn: event_id={}, order_id={}, status={}",
                event.eventId(), event.orderId(), event.status());
        emailSenderPort.send(orderStatusChangedEmailTemplate.build(event));
        log.info("Gửi email đổi trạng thái đơn thành công: event_id={}, order_id={}", event.eventId(), event.orderId());
    }

    @Override
    public void sendEmailUserRegisteredUse(UserRegisteredEvent event) {
        log.info(
                "Chuẩn bị gửi email chào mừng: event_id={}, user_id={}",
                event.eventId(),
                event.userId()
        );

        EmailMessage message = userRegisteredEmailTemplate.build(event);

        emailSenderPort.send(message);

        log.info(
                "Gửi email chào mừng thành công: event_id={}, user_id={}",
                event.eventId(),
                event.userId()
        );
    }

}
