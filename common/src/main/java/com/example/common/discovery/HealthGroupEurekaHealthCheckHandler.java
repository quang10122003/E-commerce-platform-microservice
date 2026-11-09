package com.example.common.discovery;

import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.HealthEndpointGroups;
import org.springframework.boot.health.actuate.endpoint.StatusAggregator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.cloud.netflix.eureka.EurekaHealthCheckHandler;

// Eureka chỉ xét một health group thay vì mọi indicator; kế thừa handler mặc định để khi app đang tắt không ghi đè trạng thái lên Eureka.
public class HealthGroupEurekaHealthCheckHandler extends EurekaHealthCheckHandler {

    private final HealthEndpoint healthEndpoint;
    private final String groupName;

    public HealthGroupEurekaHealthCheckHandler(HealthEndpoint healthEndpoint,
                                               HealthEndpointGroups healthEndpointGroups,
                                               String groupName) {
        super(StatusAggregator.getDefault());
        // Báo lỗi khi khởi động thay vì để instance mang trạng thái UNKNOWN và bị loại khỏi load balancer.
//        kiểm tra group name có tồn tại không.
        if (healthEndpointGroups.get(groupName) == null) {
            throw new IllegalStateException("Chưa cấu hình health group " + groupName);
        }
        this.healthEndpoint = healthEndpoint;
        this.groupName = groupName;
    }

    @Override
    protected InstanceStatus getHealthStatus() {
        HealthDescriptor descriptor = healthEndpoint.healthForPath(groupName);
        Status status = descriptor != null ? descriptor.getStatus() : Status.UNKNOWN;
        return mapToInstanceStatus(status);
    }
}
