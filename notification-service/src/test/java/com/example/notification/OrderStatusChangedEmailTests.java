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
import com.example.notification.application.strategy.event.OrderStatusChangedEventHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Kiểm tra mail báo người mua khi shop xác nhận đơn hoặc giao cho đơn vị vận chuyển.
class OrderStatusChangedEmailTests {
    private final EmailSenderPort sender = mock(EmailSenderPort.class);
    private final OrderEventApplicationService router = new OrderEventApplicationService(new KafkaEventHandlerRegistry(List.of(
            new OrderStatusChangedEventHandler(new ObjectMapperAdapter(JsonMapper.builder().build()),
                    new NotificationApplicationService(sender, mock(UserRegisteredEmailTemplate.class),
                            mock(CheckoutSucceededEmailTemplate.class), mock(OrderCancelledByShopEmailTemplate.class),
                            new OrderStatusChangedEmailTemplate("orders@shop.test", ZoneId.of("Asia/Ho_Chi_Minh")))))));

    // Payload đúng định dạng Order Service ghi vào outbox.
    private String event(String status) {
        return """
                {"eventId":"0f8e7d6c-5b4a-4c3d-9e2f-1a0b9c8d7e6f","eventType":"ORDER_STATUS_CHANGED","orderId":56,
                 "checkoutId":68,"shopName":"Shop & Co","buyerEmail":"buyer@example.com","buyerFullName":"<b>An</b>",
                 "status":"%s","changedAt":"2026-11-11T03:00:00Z"}
                """.formatted(status);
    }

    private EmailMessage sentMail() {
        ArgumentCaptor<EmailMessage> mail = ArgumentCaptor.forClass(EmailMessage.class);
        verify(sender).send(mail.capture());
        return mail.getValue();
    }

    @Test
    void confirmedOrderMail() {
        router.process("e1", "ORDER_STATUS_CHANGED", event("PENDING_PICKUP"));
        EmailMessage mail = sentMail();
        assertEquals(List.of("buyer@example.com"), mail.to());
        assertEquals("Đơn hàng #56 đã được xác nhận", mail.subject());
        assertTrue(mail.html().contains("Shop <strong>Shop &amp; Co</strong> đã xác nhận"));
        assertTrue(mail.html().contains("&lt;b&gt;An&lt;/b&gt;"));
        // 03:00 UTC là 10:00 giờ Việt Nam.
        assertTrue(mail.html().contains("11/11/2026 10:00:00 +07:00"));
    }

    @Test
    void shippingOrderMail() {
        router.process("e2", "ORDER_STATUS_CHANGED", event("SHIPPING"));
        EmailMessage mail = sentMail();
        assertEquals("Đơn hàng #56 đang được giao", mail.subject());
        assertTrue(mail.html().contains("đơn vị vận chuyển"));
    }

    @Test
    void deliveredOrderMailFromAutoCompleteJob() {
        router.process("e4", "ORDER_STATUS_CHANGED", event("DONE"));
        EmailMessage mail = sentMail();
        assertEquals("Đơn hàng #56 đã giao thành công", mail.subject());
        assertTrue(mail.html().contains("Shop <strong>Shop &amp; Co</strong> đã giao thành công đơn hàng #56."));
    }

    @Test
    void statusWithoutTemplateFailsSoKafkaMovesItToDlt() {
        assertThrows(IllegalArgumentException.class, () -> router.process("e3", "ORDER_STATUS_CHANGED", event("CANCEL")));
        verify(sender, never()).send(any());
    }
}
