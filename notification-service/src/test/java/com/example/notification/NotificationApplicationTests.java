package com.example.notification;

import com.example.notification.application.dto.event.OrderCancelledByShopNotification;
import com.example.notification.application.dto.event.OrderStatusChangedNotification;
import com.example.notification.application.registry.KafkaEventHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class NotificationApplicationTests {

	@Autowired
	private KafkaEventHandlerRegistry kafkaEventHandlerRegistry;

	@Test
	void contextLoads() {
	}

	// Router bỏ qua event không có handler, nên phải chắc event shop hủy đơn đã được đăng ký.
	@Test
	void handlesOrderCancelledByShop() {
		assertNotNull(kafkaEventHandlerRegistry.handle(OrderCancelledByShopNotification.EVENT_TYPE));
		assertNotNull(kafkaEventHandlerRegistry.handle(OrderStatusChangedNotification.EVENT_TYPE));
	}

}
