package com.example.order_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VnpayQueryResult(String responseCode, String transactionStatus, BigDecimal amount,
                               String transactionNo, String bankCode, LocalDateTime payDate) {

    private static final String SUCCESS = "00";
    private static final String TRANSACTION_NOT_FOUND = "91";
    private static final String TRANSACTION_FAILED = "02";

    public boolean isPaid() {
        return SUCCESS.equals(responseCode) && SUCCESS.equals(transactionStatus);
    }

    public boolean isUnpaid() {
        return TRANSACTION_NOT_FOUND.equals(responseCode)
                || (SUCCESS.equals(responseCode) && TRANSACTION_FAILED.equals(transactionStatus));
    }

    // Truy vấn thành công nhưng giao dịch chưa có kết quả cuối (đang xử lý, hoàn tiền...).
    public boolean isProcessing() {
        return SUCCESS.equals(responseCode) && !isPaid() && !isUnpaid();
    }
}
