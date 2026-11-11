package com.example.auth_service;
import com.example.common.security.FeignGlobalConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


// Quét cả module common để Spring thấy bean dùng chung như JwtVerifier
@SpringBootApplication(scanBasePackages = "com.example")
@EnableScheduling
@EnableFeignClients(defaultConfiguration = FeignGlobalConfig.class)
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
