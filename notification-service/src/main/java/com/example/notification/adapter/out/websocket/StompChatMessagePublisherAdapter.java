package com.example.notification.adapter.out.websocket;

import com.example.notification.application.port.out.ChatMessagePublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompChatMessagePublisherAdapter implements ChatMessagePublisherPort {

    private final SimpMessagingTemplate messagingTemplate;

}
