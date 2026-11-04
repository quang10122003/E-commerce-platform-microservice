package com.example.order_service.application.port.out.repo;

import com.example.order_service.domain.model.InboxEvent;

public interface InboxEventPort {
    boolean tryInsert(InboxEvent event);
}
