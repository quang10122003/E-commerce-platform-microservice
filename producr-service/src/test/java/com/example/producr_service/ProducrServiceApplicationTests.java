package com.example.producr_service;

import com.example.producr_service.application.dto.saga.OrderInventoryRollbackRequestedPayload;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ProducrServiceApplicationTests {

	@Autowired
	private KafkaEventHandlerRegistry kafkaEventHandlerRegistry;

	@Test
	void contextLoads() {
	}

	// Consumer bỏ qua event không có handler, nên phải chắc event hoàn kho theo order đã được đăng ký.
	@Test
	void consumesOrderInventoryRollback() {
		assertNotNull(kafkaEventHandlerRegistry.getHandler(OrderInventoryRollbackRequestedPayload.EVENT_TYPE));
	}

}
