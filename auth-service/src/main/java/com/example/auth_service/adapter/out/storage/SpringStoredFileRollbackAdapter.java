package com.example.auth_service.adapter.out.storage;

import com.example.auth_service.application.DTO.event.StoredFileRollbackEvent;
import com.example.auth_service.application.port.out.storage.StoredFile;
import com.example.auth_service.application.port.out.storage.StoredFileRollbackPort;
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
