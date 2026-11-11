package com.example.notification;

import com.example.notification.adapter.out.objectMapper.ObjectMapperAdapter;
import com.example.notification.application.dto.request.EmailMessage;
import com.example.notification.application.dto.template.CheckoutSucceededEmailTemplate;
import com.example.notification.application.dto.template.OrderCancelledByShopEmailTemplate;
import com.example.notification.application.dto.template.OrderStatusChangedEmailTemplate;
import com.example.notification.application.dto.template.UserRegisteredEmailTemplate;
import com.example.notification.application.port.out.mail.EmailSenderPort;
import com.example.notification.application.registry.KafkaEventHandlerRegistry;
import com.example.notification.application.service.NotificationApplicationService;
import com.example.notification.application.service.OrderEventApplicationService;
import com.example.notification.application.strategy.event.OrderCancelledByShopEventHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Kiểm tra mail báo người mua khi shop hủy đơn, đi từ JSON event thật của Order Service qua router Kafka.
class OrderCancelledByShopEmailTests {
    private final EmailSenderPort sender = mock(EmailSenderPort.class);
    private final OrderEventApplicationService router = new OrderEventApplicationService(new KafkaEventHandlerRegistry(List.of(
            new OrderCancelledByShopEventHandler(new ObjectMapperAdapter(JsonMapper.builder().build()),
                    new NotificationApplicationService(sender, mock(UserRegisteredEmailTemplate.class),
                            mock(CheckoutSucceededEmailTemplate.class),
                            new OrderCancelledByShopEmailTemplate("orders@shop.test", ZoneId.of("Asia/Ho_Chi_Minh")),
                            mock(OrderStatusChangedEmailTemplate.class))))));

    // Payload đúng định dạng Order Service ghi vào outbox (OffsetDateTime theo UTC để kiểm tra đổi múi giờ).
    private String event(String reason, String buyerEmail) {
        return """
                {"eventId":"8b1c6f0e-2f4c-4a8e-9d0a-0b9f4c1e2d3a","eventType":"ORDER_CANCELLED_BY_SHOP","orderId":54,
                 "checkoutId":66,"shopName":"Shop <A>","buyerEmail":%s,"buyerFullName":"Nguyễn \\"Văn\\" A",
                 "cancelReason":"%s","totalAmount":444444,"cancelledAt":"2026-11-10T18:24:00Z"}
                """.formatted(buyerEmail == null ? "null" : "\"" + buyerEmail + "\"", reason);
    }

    private EmailMessage sentMail() {
        ArgumentCaptor<EmailMessage> mail = ArgumentCaptor.forClass(EmailMessage.class);
        verify(sender).send(mail.capture());
        return mail.getValue();
    }

    @Test
    void sendsCancellationMailToBuyerWithEscapedNamesAndLocalTime() {
        router.process("e1", "ORDER_CANCELLED_BY_SHOP", event("OUT_OF_STOCK", "buyer@example.com"));

        EmailMessage mail = sentMail();
        assertEquals("orders@shop.test", mail.from());
        assertEquals(List.of("buyer@example.com"), mail.to());
        assertEquals("Đơn hàng #54 đã bị shop hủy", mail.subject());
        assertTrue(mail.html().contains("Sản phẩm đã hết hàng"));
        assertTrue(mail.html().contains("Shop <strong>Shop &lt;A&gt;</strong>"));
        assertTrue(mail.html().contains("Nguyễn &quot;Văn&quot; A"));
        assertTrue(mail.html().contains("444.444 VND"));
        // 18:24 UTC là 01:24 ngày hôm sau theo giờ Việt Nam.
        assertTrue(mail.html().contains("11/11/2026 01:24:00 +07:00"));
    }

    @Test
    void unknownReasonFallsBackToOtherLabel() {
        router.process("e2", "ORDER_CANCELLED_BY_SHOP", event("SOMETHING_NEW", "buyer@example.com"));
        assertTrue(sentMail().html().contains("Lý do: Lý do khác"));
    }

    @Test
    void missingBuyerEmailFailsSoKafkaRetries() {
        assertThrows(IllegalArgumentException.class,
                () -> router.process("e3", "ORDER_CANCELLED_BY_SHOP", event("OUT_OF_STOCK", null)));
        verify(sender, never()).send(any());
    }
}
