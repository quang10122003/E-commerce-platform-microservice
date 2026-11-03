package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.InboxEvent;

public interface InboxEventPort {
    boolean tryInsert(InboxEvent event);
}
