package com.example.notification.application.dto;

public record ReadChatMessagesResult(Long lastReadMessageId, int changedMessageCount) {
}
