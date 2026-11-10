package com.example.order_service.adapter.out.payment;

import com.example.order_service.application.dto.response.PaymentUrlResponse;
import com.example.order_service.application.dto.response.VnpayQueryResult;
import com.example.order_service.application.port.out.client.VnpayQueryPort;
import com.example.order_service.infrastructure.config.VnpayProperties;
import com.example.order_service.application.port.out.client.VnpayPaymentPort;
import com.example.order_service.application.port.out.client.VnpaySignaturePort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class VnpayPaymentAdapter implements VnpayPaymentPort, VnpaySignaturePort, VnpayQueryPort {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String SECURE_HASH = "vnp_SecureHash";
    private static final String SECURE_HASH_TYPE = "vnp_SecureHashType";
    private static final String QUERY_COMMAND = "querydr";
    private static final List<String> QUERY_RESPONSE_SIGNED_FIELDS = List.of("vnp_ResponseId", "vnp_Command",
            "vnp_ResponseCode", "vnp_Message", "vnp_TmnCode", "vnp_TxnRef", "vnp_Amount", "vnp_BankCode",
            "vnp_PayDate", "vnp_TransactionNo", "vnp_TransactionType", "vnp_TransactionStatus", "vnp_OrderInfo",
            "vnp_PromotionCode", "vnp_PromotionAmount");
    private final VnpayProperties properties;
    private final RestClient restClient;

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
        String query = buildQuery(parameters);
        return new PaymentUrlResponse(properties.paymentUrl() + "?" + query + "&vnp_SecureHash=" + sign(query),
                expiresAt);
    }

    @Override
    public boolean verifySignature(Map<String, String> parameters) {
        if (parameters == null || properties.hashSecret() == null || properties.hashSecret().isBlank()) {
            return false;
        }
        String receivedHash = parameters.get(SECURE_HASH);
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }
        // VNPay chỉ ký các tham số có giá trị, không gồm chính hai trường hash.
        Map<String, String> signedParameters = new TreeMap<>();
        parameters.forEach((String key, String value) -> {
            if (!SECURE_HASH.equals(key) && !SECURE_HASH_TYPE.equals(key) && value != null && !value.isEmpty()) {
                signedParameters.put(key, value);
            }
        });
        String expectedHash = sign(buildQuery(signedParameters));
        // sánh hai chuỗi hash: hash mình tự tính lại (expectedHash) và hash VNPay gửi về
//        dùng MessageDigest.isEqual để k lộ time so sánh
        return MessageDigest.isEqual(expectedHash.getBytes(StandardCharsets.US_ASCII),
                receivedHash.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }
    @Override
    public VnpayQueryResult queryTransaction(String transactionReference, LocalDateTime transactionDate,
                                             LocalDateTime now) {
        validateConfiguration();
        requireHttpUrl(properties.queryUrl());
        Map<String, String> request = buildQueryRequest(transactionReference, transactionDate, now);
        Map<String, String> response = restClient.post()
                .uri(properties.queryUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, String>>() {
                });
        if (response == null) {
            throw new IllegalStateException("QueryDR VNPay không trả dữ liệu");
        }
        VnpayQueryResult result = new VnpayQueryResult(
                valueOf(response, "vnp_ResponseCode"),
                valueOf(response, "vnp_TransactionStatus"),
                parseAmount(valueOf(response, "vnp_Amount")),
                valueOf(response, "vnp_TransactionNo"),
                valueOf(response, "vnp_BankCode"),
                parseDate(valueOf(response, "vnp_PayDate")));
        // VNPay không ký phản hồi lỗi (ví dụ 99); chỉ bắt buộc chữ ký khi kết quả dẫn tới DONE hoặc CANCEL.
        if (response.containsKey(SECURE_HASH) || result.isPaid() || result.isUnpaid()) {
            verifyQueryResponse(response);
        }
        return result;
    }

    private Map<String, String> buildQueryRequest(String transactionReference, LocalDateTime transactionDate,
                                                  LocalDateTime now) {
        Map<String, String> request = new LinkedHashMap<>();
        request.put("vnp_RequestId", UUID.randomUUID().toString().replace("-", ""));
        request.put("vnp_Version", properties.version());
        request.put("vnp_Command", QUERY_COMMAND);
        request.put("vnp_TmnCode", properties.tmnCode());
        request.put("vnp_TxnRef", transactionReference);
        request.put("vnp_TransactionDate", transactionDate.format(DATE_FORMAT));
        request.put("vnp_CreateDate", now.format(DATE_FORMAT));
        request.put("vnp_IpAddr", serverIpAddress());
        request.put("vnp_OrderInfo", "Truy van giao dich " + transactionReference);
        // VNPay quy định chữ ký QueryDR nối các giá trị bằng "|" theo đúng thứ tự đã put ở trên.
        request.put(SECURE_HASH, sign(String.join("|", request.values())));
        return request;
    }

    private void verifyQueryResponse(Map<String, String> response) {
        String signedData = QUERY_RESPONSE_SIGNED_FIELDS.stream()
                .map((String field) -> response.getOrDefault(field, ""))
                .collect(Collectors.joining("|"));
        String receivedHash = response.getOrDefault(SECURE_HASH, "").toLowerCase(Locale.ROOT);
        if (!MessageDigest.isEqual(sign(signedData).getBytes(StandardCharsets.US_ASCII),
                receivedHash.getBytes(StandardCharsets.US_ASCII))) {
            throw new IllegalStateException("QueryDR VNPay sai chữ ký phản hồi");
        }
    }

    private String valueOf(Map<String, String> response, String field) {
        String value = response.get(field);
        return value == null || value.isEmpty() ? null : value;
    }

    // VNPay gửi số tiền đã nhân 100.
    private BigDecimal parseAmount(String value) {
        return value == null || !value.matches("\\d{1,15}") ? null : new BigDecimal(value).movePointLeft(2);
    }

    private LocalDateTime parseDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private String serverIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException exception) {
            return "127.0.0.1";
        }
    }

    private String buildQuery(Map<String, String> sortedParameters) {
        return sortedParameters.entrySet().stream()
                .map((Map.Entry<String, String> entry) -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
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
