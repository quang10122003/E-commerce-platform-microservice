package com.example.common.security;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
//cấu hình  Feign Client  Forward JWT từ request hiện tại sang mọi request do Feign tạo.
@Configuration
public class FeignGlobalConfig {


    @Bean
    public RequestInterceptor authorizationInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String authorization =
                    request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authorization != null && !authorization.isBlank()) {
                requestTemplate.header(
                        HttpHeaders.AUTHORIZATION,
                        authorization
                );
            }
        };
    }
}