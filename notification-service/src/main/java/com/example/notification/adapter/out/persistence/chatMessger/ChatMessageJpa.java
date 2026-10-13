package com.example.notification.adapter.out.persistence.chatMessger;

import com.example.notification.adapter.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageJpa extends JpaRepository<ChatMessageEntity, Long> {
}
