package com.example.producr_service.adapter.out.storage;

import com.example.producr_service.application.dto.event.ProductImageRollbackEvent;
import com.example.producr_service.application.port.out.storage.ProductImageRollbackPort;
import com.example.producr_service.application.port.out.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpringProductImageRollbackAdapter implements ProductImageRollbackPort {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void deleteImageStore(List<StoredFile> storedFiles) {
        eventPublisher.publishEvent(new ProductImageRollbackEvent(storedFiles));
    }
}
