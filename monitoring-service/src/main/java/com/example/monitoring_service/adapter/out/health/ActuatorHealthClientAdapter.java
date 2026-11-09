package com.example.monitoring_service.adapter.out.health;

import com.example.monitoring_service.adapter.out.health.dto.ActuatorComponentResponse;
import com.example.monitoring_service.adapter.out.health.dto.ActuatorHealthResponse;
import com.example.monitoring_service.application.dto.HealthGroupResult;
import com.example.monitoring_service.application.port.out.HealthGroupClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ActuatorHealthClientAdapter implements HealthGroupClientPort {

    private final RestClient healthRestClient;

    public ActuatorHealthClientAdapter(@Qualifier("healthRestClient") RestClient healthRestClient) {
        this.healthRestClient = healthRestClient;
    }

    // call api của spring actuator theo group đã cấu hình  trả DTO HealthGroupResult
    @Override
    public HealthGroupResult fetchHealthGroup(String baseUrl, String groupName) {
        try {
            // Dùng exchange để tự đọc body: actuator trả 503 kèm JSON khi DOWN, retrieve sẽ ném lỗi và mất dữ liệu.
            return healthRestClient.get()
                    .uri(baseUrl + "/actuator/health/{group}", groupName)
                    .exchange((request, response) -> {
                        HttpStatusCode statusCode = response.getStatusCode();
                        // ko gọi đc api 404
                        if (statusCode.isSameCodeAs(HttpStatus.NOT_FOUND)) {
                            return HealthGroupResult.notConfigured();
                        }
                        // ok or 503
                        if (!statusCode.isSameCodeAs(HttpStatus.OK)
                                && !statusCode.isSameCodeAs(HttpStatus.SERVICE_UNAVAILABLE)) {
                            log.warn("Health endpoint trả mã không mong đợi: base_url={}, group={}, status={}",
                                    baseUrl, groupName, statusCode.value());
                            return HealthGroupResult.unreachable();
                        }
                        ActuatorHealthResponse body = response.bodyTo(ActuatorHealthResponse.class);
                        return toResult(body);
                    });
        } catch (RestClientException exception) {
            // lỗi thì bắt và trả dữ liệu với hàm unreachable khi api check healt timeout,mã HTTP lạ
            log.warn("Không gọi được health endpoint: base_url={}, group={}", baseUrl, groupName);
            return HealthGroupResult.unreachable();
        }
    }

    // chuyển repone api về HealthGroupResult
    private HealthGroupResult toResult(ActuatorHealthResponse body) {
        if (body == null) {
            return HealthGroupResult.unreachable();
        }
        Map<String, String> components = new HashMap<>();
        if (body.components() != null) {
            for (Map.Entry<String, ActuatorComponentResponse> entry : body.components().entrySet()) {
                ActuatorComponentResponse component = entry.getValue();
                String status = component == null || component.status() == null ? "UNKNOWN" : component.status();
                components.put(entry.getKey(), status);
            }
        }
        return HealthGroupResult.available(body.status(), components);
    }
}
