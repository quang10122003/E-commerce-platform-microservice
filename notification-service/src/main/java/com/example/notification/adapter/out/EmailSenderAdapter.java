package com.example.notification.adapter.out;

import com.example.notification.application.dto.request.EmailMessage;
import com.example.notification.application.port.out.EmailSenderPort;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Slf4j 
@Component 
@RequiredArgsConstructor
public class EmailSenderAdapter implements EmailSenderPort {
    private final RestClient restClient;
    @Value("${app.resend.resend_key}")
    String apiKey;
    @Override
    public void send(EmailMessage message) {
        try {
            restClient.post()
                    .uri("https://api.resend.com/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
            log.info("gửi email {} cho email:{} thành công", message.subject(), message.to());
        } catch (Exception e) {
            log.error("gửi email {} cho email:{} thất bại", message.to(), message.to(), e);
        }
    }
    
}
