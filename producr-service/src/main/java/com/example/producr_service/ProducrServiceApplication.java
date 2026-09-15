package com.example.producr_service;

import com.example.common.security.FeignGlobalConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.example")
@EnableDiscoveryClient
@EnableFeignClients(defaultConfiguration = FeignGlobalConfig.class)
public class ProducrServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProducrServiceApplication.class, args);
	}

}
