package com.example.notification.application.service;

import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;
import com.example.notification.application.port.in.SendEmailUserRegisteredUseCase;
import com.example.notification.application.port.out.EmailSenderPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class NotificationApplicationService
        implements SendEmailUserRegisteredUseCase {

    private final EmailSenderPort emailSenderPort;
    private final UserRegisteredEmailTemplate userRegisteredEmailTemplate;

    @Override
    public void sendEmailUserRegisteredUse(UserRegisteredEvent event) {
        log.info(
                "Chuẩn bị gửi email chào mừng: event_id={}, user_id={}",
                event.eventId(),
                event.userId()
        );

        var message = userRegisteredEmailTemplate.build(event);

        emailSenderPort.send(message);

        log.info(
                "Gửi email chào mừng thành công: event_id={}, user_id={}",
                event.eventId(),
                event.userId()
        );
    }

}
