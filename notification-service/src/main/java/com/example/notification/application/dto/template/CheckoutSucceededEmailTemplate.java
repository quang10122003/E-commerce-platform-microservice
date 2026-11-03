package com.example.notification.application.dto.template;

import com.example.common.untill.ValidationUtils;
import com.example.notification.application.dto.event.CheckoutSucceededNotification;
import com.example.notification.application.dto.request.EmailMessage;
import com.example.notification.application.dto.template.interfaces.EmailTemplate;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class CheckoutSucceededEmailTemplate implements EmailTemplate<CheckoutSucceededNotification> {
    private final String orderSender;
    private final ZoneId zoneId;

    public CheckoutSucceededEmailTemplate(String orderSender, ZoneId zoneId) {
        if (!ValidationUtils.hasText(orderSender)) {
            throw new IllegalArgumentException("Địa chỉ gửi email đơn hàng không được trống");
        }
        this.orderSender = orderSender;
        this.zoneId = Objects.requireNonNull(zoneId);
    }

    @Override
    public EmailMessage build(CheckoutSucceededNotification event) {
        if (!ValidationUtils.hasText(event.buyerEmail()) || !ValidationUtils.hasText(event.buyerFullName())) {
            throw new IllegalArgumentException("Event checkout thiếu email hoặc tên người mua");
        }
        String paymentMethod = switch (event.paymentMethod()) {
            case "COD" -> "Thanh toán khi nhận hàng";
            case "VNPAY" -> "Thanh toán qua ngân hàng";
            default -> throw new IllegalArgumentException("Phương thức thanh toán không được hỗ trợ");
        };
        String buyerName = event.buyerFullName().replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
        String orderIds = event.orderIds().stream().map(String::valueOf).collect(Collectors.joining(", "));
        String totalAmount = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"))
                .format(event.totalAmount());
        String occurredAt = event.occurredAt().atZoneSameInstant(zoneId)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss XXX"));
        String html = """
                <html lang="vi">
                    <body>
                        <h2>Đặt hàng thành công</h2>
                        <p>Xin chào %s, cảm ơn bạn đã đặt hàng!</p>
                        <p>Mã checkout: %s</p>
                        <p>Mã đơn hàng: %s</p>
                        <p>Phương thức thanh toán: %s</p>
                        <p>Tổng tiền: %s VND</p>
                        <p>Thời gian đặt hàng: %s</p>
                    </body>
                </html>
                """.formatted(buyerName, event.checkoutId(), orderIds, paymentMethod, totalAmount, occurredAt);
        return new EmailMessage(orderSender, List.of(event.buyerEmail()),
                "Đặt hàng thành công - Checkout #" + event.checkoutId(), html);
    }
}
