package com.example.notification.application.dto.template;

import com.example.common.untill.ValidationUtils;
import com.example.notification.application.dto.event.OrderStatusChangedNotification;
import com.example.notification.application.dto.request.EmailMessage;
import com.example.notification.application.dto.template.interfaces.EmailTemplate;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

// Mail báo người mua đơn đã được xác nhận, đang giao hoặc đã giao thành công.
public class OrderStatusChangedEmailTemplate implements EmailTemplate<OrderStatusChangedNotification> {
    private final String orderSender;
    private final ZoneId zoneId;

    public OrderStatusChangedEmailTemplate(String orderSender, ZoneId zoneId) {
        if (!ValidationUtils.hasText(orderSender)) {
            throw new IllegalArgumentException("Địa chỉ gửi email đơn hàng không được trống");
        }
        this.orderSender = orderSender;
        this.zoneId = Objects.requireNonNull(zoneId);
    }

    @Override
    public EmailMessage build(OrderStatusChangedNotification event) {
        if (!ValidationUtils.hasText(event.buyerEmail()) || !ValidationUtils.hasText(event.buyerFullName())) {
            throw new IllegalArgumentException("Event đổi trạng thái đơn thiếu email hoặc tên người mua");
        }
        // Chỉ các bước shop chuyển hoặc job tự hoàn thành mới có mail; trạng thái khác là lỗi dữ liệu để Kafka đưa vào DLT.
        String[] content = switch (event.status()) {
            case "PENDING_PICKUP" -> new String[] {"đã được xác nhận", "đã xác nhận và đang chuẩn bị hàng cho đơn"};
            case "SHIPPING" -> new String[] {"đang được giao", "đã giao đơn cho đơn vị vận chuyển, đơn đang trên đường tới bạn:"};
            case "DONE" -> new String[] {"đã giao thành công", "đã giao thành công đơn hàng"};
            default -> throw new IllegalArgumentException("Trạng thái đơn không có mẫu email: " + event.status());
        };
        String changedAt = event.changedAt().atZoneSameInstant(zoneId).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss XXX"));
        String html = """
                <html lang="vi">
                    <body>
                        <h2>Đơn hàng #%s %s</h2>
                        <p>Xin chào %s,</p>
                        <p>Shop <strong>%s</strong> %s #%s.</p>
                        <p>Thời gian cập nhật: %s</p>
                    </body>
                </html>
                """.formatted(event.orderId(), content[0], escape(event.buyerFullName()), escape(event.shopName()),
                content[1], event.orderId(), changedAt);
        return new EmailMessage(orderSender, List.of(event.buyerEmail()), "Đơn hàng #" + event.orderId() + " " + content[0], html);
    }

    // Tên người mua và tên shop do người dùng nhập nên phải escape trước khi chèn vào HTML.
    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
