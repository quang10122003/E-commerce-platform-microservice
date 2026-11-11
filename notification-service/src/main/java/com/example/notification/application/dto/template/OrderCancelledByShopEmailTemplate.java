package com.example.notification.application.dto.template;

import com.example.common.untill.ValidationUtils;
import com.example.notification.application.dto.event.OrderCancelledByShopNotification;
import com.example.notification.application.dto.request.EmailMessage;
import com.example.notification.application.dto.template.interfaces.EmailTemplate;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

// Mail báo người mua đơn hàng bị shop hủy, kèm lý do và thời điểm hủy theo múi giờ hệ thống.
public class OrderCancelledByShopEmailTemplate implements EmailTemplate<OrderCancelledByShopNotification> {
    // Nhãn lý do theo enum OrderCancelReason phía Order Service; lý do lạ hiển thị "Lý do khác".
    private static final Map<String, String> REASON_LABELS = Map.of(
            "OUT_OF_STOCK", "Sản phẩm đã hết hàng",
            "CANNOT_DELIVER", "Shop không thể giao hàng tới địa chỉ của bạn",
            "BUYER_UNREACHABLE", "Shop không liên hệ được với bạn",
            "PRODUCT_INFO_ERROR", "Sai giá hoặc thông tin sản phẩm",
            "OTHER", "Lý do khác");

    private final String orderSender;
    private final ZoneId zoneId;

    public OrderCancelledByShopEmailTemplate(String orderSender, ZoneId zoneId) {
        if (!ValidationUtils.hasText(orderSender)) {
            throw new IllegalArgumentException("Địa chỉ gửi email đơn hàng không được trống");
        }
        this.orderSender = orderSender;
        this.zoneId = Objects.requireNonNull(zoneId);
    }

    @Override
    public EmailMessage build(OrderCancelledByShopNotification event) {
        if (!ValidationUtils.hasText(event.buyerEmail()) || !ValidationUtils.hasText(event.buyerFullName())) {
            throw new IllegalArgumentException("Event shop hủy đơn thiếu email hoặc tên người mua");
        }
        String reason = REASON_LABELS.getOrDefault(event.cancelReason(), "Lý do khác");
        String totalAmount = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(event.totalAmount());
        String cancelledAt = event.cancelledAt().atZoneSameInstant(zoneId)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss XXX"));
        String html = """
                <html lang="vi">
                    <body>
                        <h2>Đơn hàng #%s đã bị hủy</h2>
                        <p>Xin chào %s,</p>
                        <p>Shop <strong>%s</strong> đã hủy đơn hàng #%s của bạn.</p>
                        <p>Lý do: %s</p>
                        <p>Giá trị đơn hàng: %s VND (thanh toán khi nhận hàng, bạn không bị trừ tiền)</p>
                        <p>Thời gian hủy: %s</p>
                    </body>
                </html>
                """.formatted(event.orderId(), escape(event.buyerFullName()), escape(event.shopName()), event.orderId(),
                reason, totalAmount, cancelledAt);
        return new EmailMessage(orderSender, List.of(event.buyerEmail()), "Đơn hàng #" + event.orderId() + " đã bị shop hủy", html);
    }

    // Tên người mua và tên shop do người dùng nhập nên phải escape trước khi chèn vào HTML.
    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
