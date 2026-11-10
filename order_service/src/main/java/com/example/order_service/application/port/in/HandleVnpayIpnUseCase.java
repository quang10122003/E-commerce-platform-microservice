package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.VnpayIpnResponse;

import java.util.Map;

public interface HandleVnpayIpnUseCase {
    VnpayIpnResponse handleIpn(Map<String, String> parameters);
}
