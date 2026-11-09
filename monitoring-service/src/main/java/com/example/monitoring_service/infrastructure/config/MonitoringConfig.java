package com.example.monitoring_service.infrastructure.config;

import com.example.monitoring_service.application.port.out.HealthGroupClientPort;
import com.example.monitoring_service.application.port.out.ServiceRegistryPort;
import com.example.monitoring_service.application.service.SystemStatusService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.util.concurrent.Executor;

@Configuration
public class MonitoringConfig {

    // Tạo RestClient
    @Bean
    RestClient healthRestClient(MonitoringProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.requestTimeout());
        requestFactory.setReadTimeout(properties.requestTimeout());
        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor(forwardAuthorizationInterceptor())
                .build();
    }

    // Actuator của các service yêu cầu quyền admin nên chuyển tiếp token của admin đang gọi API, giống Feign.
    private ClientHttpRequestInterceptor forwardAuthorizationInterceptor() {
        return (request, body, execution) -> {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletAttributes) {
                HttpServletRequest currentRequest = servletAttributes.getRequest();
                String authorization = currentRequest.getHeader(HttpHeaders.AUTHORIZATION);
                if (authorization != null && !authorization.isBlank()) {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorization);
                }
            }
            return execution.execute(request, body);
        };
    }

    @Bean
    SystemStatusService getSystemStatusUseCase(ServiceRegistryPort serviceRegistryPort,
                                               HealthGroupClientPort healthGroupClientPort,
                                               @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor,
                                               Clock clock,
                                               MonitoringProperties properties) {
        return new SystemStatusService(serviceRegistryPort, healthGroupClientPort,
                propagateRequestContext(applicationTaskExecutor), clock, properties.expectedServices());
    }

    // Health check chạy trên luồng khác nên phải mang request context theo, nếu không interceptor không đọc được token.
    private Executor propagateRequestContext(Executor delegate) {
        return task -> {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            delegate.execute(() -> {
                RequestContextHolder.setRequestAttributes(attributes);
                try {
                    task.run();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            });
        };
    }
}
