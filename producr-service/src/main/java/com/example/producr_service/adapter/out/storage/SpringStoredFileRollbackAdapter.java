package com.example.producr_service.adapter.out.storage;

import com.example.producr_service.application.dto.event.StoredFileRollbackEvent;
import com.example.producr_service.application.port.out.storage.StoredFileRollbackPort;
import com.example.producr_service.application.port.out.storage.StoredFile;
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
