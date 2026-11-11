package com.example.order_service;

import com.example.order_service.application.dto.event.OrderCancelledByShopPayload;
import com.example.order_service.application.dto.event.OrderStatusChangedPayload;
import com.example.order_service.application.dto.saga.OrderInventoryRollbackRequestedPayload;
import com.example.order_service.application.registry.OutboxEventHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class OrderServiceApplicationTests {

	@Autowired
	private OutboxEventHandlerRegistry outboxEventHandlerRegistry;

	@Test
	void contextLoads() {
	}

	// Event mới ghi vào outbox phải có handler publish, nếu không sẽ kẹt PENDING và retry mãi.
	@Test
	void outboxPublishesOrderInventoryRollback() {
		assertDoesNotThrow(() -> outboxEventHandlerRegistry.handle(OrderInventoryRollbackRequestedPayload.EVENT_TYPE));
		assertDoesNotThrow(() -> outboxEventHandlerRegistry.handle(OrderCancelledByShopPayload.EVENT_TYPE));
		assertDoesNotThrow(() -> outboxEventHandlerRegistry.handle(OrderStatusChangedPayload.EVENT_TYPE));
	}

}
