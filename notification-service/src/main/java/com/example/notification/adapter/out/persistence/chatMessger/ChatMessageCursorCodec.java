package com.example.notification.adapter.out.persistence.chatMessger;

import com.example.common.exception.BusinessException;
import com.example.notification.application.error.ChatError;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

@Component
public class ChatMessageCursorCodec {

    public String encode(Long conversationId, LocalDateTime createdAt, Long messageId) {
        String value = conversationId + "|" + createdAt + "|" + messageId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public ChatMessageCursor decode(String encodedCursor, Long conversationId) {
        try {
            String value = new String(Base64.getUrlDecoder().decode(encodedCursor), StandardCharsets.UTF_8);
            String[] parts = value.split("\\|", -1);
            if (parts.length != 3 || !conversationId.equals(Long.valueOf(parts[0]))) {
                throw new IllegalArgumentException();
            }
            LocalDateTime createdAt = LocalDateTime.parse(parts[1]);
            Long messageId = Long.valueOf(parts[2]);
            if (messageId <= 0) {
                throw new IllegalArgumentException();
            }
            return new ChatMessageCursor(createdAt, messageId);
        } catch (IllegalArgumentException | DateTimeParseException exception) {
            throw new BusinessException(ChatError.CHAT_CURSOR_INVALID);
        }
    }

    public record ChatMessageCursor(LocalDateTime createdAt, Long messageId) {
    }
}
