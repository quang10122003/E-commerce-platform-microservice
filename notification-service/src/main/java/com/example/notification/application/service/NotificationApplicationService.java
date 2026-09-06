package com.example.notification.application.service;

import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;
import com.example.notification.application.port.in.SendEmailUserRegisteredUseCase;
import com.example.notification.application.port.out.EmailSenderPort;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationApplicationService
        implements SendEmailUserRegisteredUseCase {

    private final EmailSenderPort emailSenderPort;
    private final UserRegisteredEmailTemplate userRegisteredEmailTemplate;

    @Override
    public void sendEmailUserRegisteredUse(UserRegisteredEvent event) {
        var message = userRegisteredEmailTemplate.build(event);

        emailSenderPort.send(message);
    }

}
