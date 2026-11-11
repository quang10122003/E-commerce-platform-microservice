package com.example.auth_service;

import com.example.auth_service.application.port.in.GetMyShopInfoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AuthServiceApplicationTests {

	@Autowired
	private GetMyShopInfoUseCase getMyShopInfoUseCase;

	@Test
	void contextLoads() {
	}

	// Controller inject use case qua interface; thiếu bean chỉ lộ ra lúc khởi động nên kiểm tra ở đây.
	@Test
	void myShopInfoUseCaseIsWired() {
		assertNotNull(getMyShopInfoUseCase);
	}

}
