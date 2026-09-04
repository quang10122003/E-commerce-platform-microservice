package com.example.auth_service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


// Quét cả module common để Spring thấy bean dùng chung như JwtVerifier
@SpringBootApplication(scanBasePackages = "com.example")
@EnableScheduling
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
