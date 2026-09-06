package com.example.notification.application.port.out;

import com.example.notification.application.dto.request.EmailMessage;

public interface EmailSenderPort {
    void send(EmailMessage message);
}
