package com.example.notification.application.service;

import com.example.notification.application.dto.request.EmailMessage;
import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.dto.event.CheckoutSucceededNotification;
import com.example.notification.application.dto.template.CheckoutSucceededEmailTemplate;
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

    public void sendCheckoutSucceededEmail(CheckoutSucceededNotification event) {
        log.info("Chuẩn bị gửi email đặt hàng thành công: event_id={}, checkout_id={}",
                event.eventId(), event.checkoutId());
        EmailMessage message = checkoutSucceededEmailTemplate.build(event);
        emailSenderPort.send(message);
        log.info("Gửi email đặt hàng thành công: event_id={}, checkout_id={}",
                event.eventId(), event.checkoutId());
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
