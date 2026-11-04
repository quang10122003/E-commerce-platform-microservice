package com.example.order_service.application.service;

import com.example.order_service.application.dto.response.PaymentUrlResponse;
import com.example.order_service.application.port.out.client.VnpayPaymentPort;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class VnpayPaymentService {

    private final VnpayPaymentPort vnpayPaymentPort;
    private final PaymentRepoPort paymentRepoPort;

    @Transactional
    public String startPayment(Payment payment, Checkout checkout, LocalDateTime now) {
        String reference = UUID.randomUUID().toString().replace("-", "");
        PaymentUrlResponse response = vnpayPaymentPort.createPaymentUrl(reference, checkout.getTotalAmount(),
                checkout.getClientIp(), now);
        payment.prepare(checkout.getTotalAmount(), reference, response.expiresAt(), now);
        payment.start(response.url(), now);
        paymentRepoPort.save(payment);
        return payment.getPaymentUrl();
    }
}
