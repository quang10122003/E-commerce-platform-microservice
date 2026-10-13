package com.example.notification.adapter.out.persistence.ChatConversation;

import com.example.notification.adapter.entity.ChatConversationsEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChatConversationJpa extends JpaRepository<ChatConversationsEntity, Long> {

}
