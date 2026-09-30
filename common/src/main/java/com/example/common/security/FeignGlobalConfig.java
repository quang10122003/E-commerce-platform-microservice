package com.example.common.security;

import com.example.common.web.RequestIdFilter;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Chuyển tiếp header Authorization và X-Request-Id sang request Feign.
@Configuration
public class FeignGlobalConfig {


    @Bean
    public RequestInterceptor requestContextInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            String requestId = MDC.get("request_id");
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization != null && !authorization.isBlank()) {
                    requestTemplate.header(HttpHeaders.AUTHORIZATION, authorization);
                }

                if (requestId == null || requestId.isBlank()) {
                    requestId = request.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
                }
            }

            if (requestId != null && !requestId.isBlank()) {
                requestTemplate.header(RequestIdFilter.REQUEST_ID_HEADER, requestId);
            }
        };
    }
}
