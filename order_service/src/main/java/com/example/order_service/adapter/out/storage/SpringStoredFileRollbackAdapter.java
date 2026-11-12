package com.example.order_service.adapter.out.storage;

import com.example.order_service.application.dto.event.StoredFileRollbackEvent;
import com.example.order_service.application.port.out.storage.StoredFile;
import com.example.order_service.application.port.out.storage.StoredFileRollbackPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpringStoredFileRollbackAdapter implements StoredFileRollbackPort {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void deleteImageStore(List<StoredFile> storedFiles) {
        eventPublisher.publishEvent(new StoredFileRollbackEvent(storedFiles));
    }
}
