package com.example.notification.application.port.in;

import com.example.notification.application.dto.event.UserRegisteredEvent;

public interface SendEmailUserRegisteredUseCase {
     void sendEmailUserRegisteredUse(UserRegisteredEvent event);
}
