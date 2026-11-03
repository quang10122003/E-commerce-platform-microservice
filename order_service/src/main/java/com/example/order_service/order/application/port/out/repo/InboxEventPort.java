package com.example.order_service.order.application.port.out.repo;

import com.example.order_service.order.domain.model.InboxEvent;

public interface InboxEventPort {
    boolean tryInsert(InboxEvent event);
}
