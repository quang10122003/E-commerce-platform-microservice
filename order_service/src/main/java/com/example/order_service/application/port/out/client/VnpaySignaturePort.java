package com.example.order_service.application.port.out.client;

import java.util.Map;

public interface VnpaySignaturePort {
    boolean verifySignature(Map<String, String> parameters);
}
