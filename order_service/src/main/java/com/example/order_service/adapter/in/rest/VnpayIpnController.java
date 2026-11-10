package com.example.order_service.adapter.in.rest;

import com.example.order_service.application.dto.response.VnpayIpnResponse;
import com.example.order_service.application.port.in.HandleVnpayIpnUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class VnpayIpnController {

    private final HandleVnpayIpnUseCase handleVnpayIpnUseCase;

    // VNPay đọc đúng khóa RspCode/Message viết hoa, nên không trả record trực tiếp.
    // Bắt lỗi tại đây vì transaction của use case đã rollback; VNPay chỉ hiểu RspCode, không hiểu ApiResponse.
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> handleIpn(@RequestParam Map<String, String> parameters) {
        VnpayIpnResponse response;
        try {
            response = handleVnpayIpnUseCase.handleIpn(parameters);
        } catch (Exception exception) {
            log.error("Xử lý IPN VNPay thất bại, cần kiểm tra: vnp_TxnRef={}, error_type={}, error={}",
                    parameters.get("vnp_TxnRef"), exception.getClass().getSimpleName(), exception.getMessage());
            response = new VnpayIpnResponse("99", "Unknown error");
        }
        Map<String, String> body = new LinkedHashMap<>();
        body.put("RspCode", response.rspCode());
        body.put("Message", response.message());
        return ResponseEntity.ok(body);
    }
}
