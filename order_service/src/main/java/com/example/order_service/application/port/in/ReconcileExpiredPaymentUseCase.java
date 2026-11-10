package com.example.order_service.application.port.in;

import java.util.List;

public interface ReconcileExpiredPaymentUseCase {
    List<String> findExpiredPendingReferences();
    void reconcile(String transactionReference);
}
