package com.example.notification.application.dto.request;

import java.util.List;
public record EmailMessage(
        String from,
        List<String> to,
        String subject,
        String html
) {
}