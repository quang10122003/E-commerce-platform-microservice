package com.example.producr_service.adapter.in.listener;

import com.example.producr_service.application.dto.event.StoredFileRollbackEvent;
import com.example.producr_service.application.port.in.DeleteStoredFilesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoredFileRollbackListener {
    private final DeleteStoredFilesUseCase deleteStoredFilesUseCase;

    // Dọn ảnh đã upload sau khi transaction DB rollback, kể cả lỗi phát sinh lúc commit.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void cleanupImageStore(StoredFileRollbackEvent event) {
        try {
            deleteStoredFilesUseCase.deleteStoredFiles(event.storedFiles());
        } catch (RuntimeException exception) {
            log.error("Không thể dọn file đã upload sau khi transaction rollback", exception);
        }
    }
}
