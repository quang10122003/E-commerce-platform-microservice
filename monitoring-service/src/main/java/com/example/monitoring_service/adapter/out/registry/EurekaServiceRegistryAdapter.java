package com.example.monitoring_service.adapter.out.registry;

import com.example.monitoring_service.application.dto.RegisteredInstance;
import com.example.monitoring_service.application.port.out.ServiceRegistryPort;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class EurekaServiceRegistryAdapter implements ServiceRegistryPort {

    private final EurekaClient eurekaClient;

    // lấy danh sách các các instance của các service
    @Override
    public List<RegisteredInstance> findAllInstances() {
        List<RegisteredInstance> instances = new ArrayList<>();
        for (Application application : eurekaClient.getApplications().getRegisteredApplications()) {
            for (InstanceInfo instance : application.getInstances()) {
                instances.add(new RegisteredInstance(
                        application.getName().toLowerCase(Locale.ROOT),
                        instance.getInstanceId(),
                        toBaseUrl(instance),
                        instance.getStatus().name()));
            }
        }
        return instances;
    }

    // Eureka lưu tên service dạng chữ hoa và home page có dấu "/" cuối, cần chuẩn hóa trước khi ghép đường dẫn actuator.
    private String toBaseUrl(InstanceInfo instance) {
        String homePageUrl = instance.getHomePageUrl();
        return homePageUrl.endsWith("/") ? homePageUrl.substring(0, homePageUrl.length() - 1) : homePageUrl;
    }
}
