package com.example.order_service.adapter.out.payment;

import com.example.order_service.application.dto.response.PaymentUrlResponse;
import com.example.order_service.infrastructure.config.VnpayProperties;
import com.example.order_service.application.port.out.client.VnpayPaymentPort;
import lombok.RequiredArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class VnpayPaymentAdapter implements VnpayPaymentPort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final VnpayProperties properties;

    @Override
    public PaymentUrlResponse createPaymentUrl(String transactionReference, BigDecimal amount,
                                               String clientIp, LocalDateTime now) {
        validateConfiguration();
        if (transactionReference == null || !transactionReference.matches("[a-zA-Z0-9]{1,100}")
                || amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0
                || amount.movePointRight(2).compareTo(new BigDecimal("999999999999")) > 0
                || clientIp == null || clientIp.length() < 7 || clientIp.length() > 45
                || !clientIp.matches("[0-9a-fA-F:.]+") || now == null) {
            throw new IllegalArgumentException("Thông tin tạo URL thanh toán VNPay không hợp lệ");
        }
        LocalDateTime expiresAt = now.plusMinutes(properties.timeoutMinutes());
        Map<String, String> parameters = new TreeMap<>();
        parameters.put("vnp_Version", properties.version());
        parameters.put("vnp_Command", properties.command());
        parameters.put("vnp_TmnCode", properties.tmnCode());
        parameters.put("vnp_Amount", amount.movePointRight(2).toBigIntegerExact().toString());
        parameters.put("vnp_CurrCode", properties.currency());
        parameters.put("vnp_TxnRef", transactionReference);
        parameters.put("vnp_OrderInfo", "Thanh toan don hang " + transactionReference);
        parameters.put("vnp_OrderType", properties.orderType());
        parameters.put("vnp_Locale", properties.locale());
        parameters.put("vnp_ReturnUrl", properties.returnUrl());
        parameters.put("vnp_IpAddr", clientIp);
        parameters.put("vnp_CreateDate", now.format(DATE_FORMAT));
        parameters.put("vnp_ExpireDate", expiresAt.format(DATE_FORMAT));
        String query = parameters.entrySet().stream()
                .map((Map.Entry<String, String> entry) -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return new PaymentUrlResponse(properties.paymentUrl() + "?" + query + "&vnp_SecureHash=" + sign(query),
                expiresAt);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(properties.hashSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Không thể ký URL thanh toán VNPay", exception);
        }
    }

    private void validateConfiguration() {
        if (properties.tmnCode() == null || !properties.tmnCode().matches("[a-zA-Z0-9]{8}")
                || properties.hashSecret() == null || properties.hashSecret().isBlank()
                || !"2.1.0".equals(properties.version()) || !"pay".equals(properties.command())
                || !"VND".equals(properties.currency())
                || !("vn".equals(properties.locale()) || "en".equals(properties.locale()))
                || properties.orderType() == null || !properties.orderType().matches("[a-zA-Z0-9]{1,100}")
                || properties.timeoutMinutes() < 1 || properties.timeoutMinutes() > 1440) {
            throw new IllegalStateException("Cấu hình VNPay không hợp lệ hoặc thiếu thông tin merchant");
        }
        URI paymentUri = requireHttpUrl(properties.paymentUrl());
        requireHttpUrl(properties.returnUrl());
        if (paymentUri.getRawQuery() != null || paymentUri.getFragment() != null) {
            throw new IllegalStateException("URL cổng thanh toán VNPay không được chứa query hoặc fragment");
        }
    }

    private URI requireHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình URL VNPay");
        }
        URI uri = URI.create(value);
        if (!("https".equals(uri.getScheme()) || "http".equals(uri.getScheme())) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getFragment() != null) {
            throw new IllegalStateException("URL VNPay không hợp lệ");
        }
        return uri;
    }
}
