package com.example.monitoring_service.application.service;

import com.example.monitoring_service.application.constant.HealthGroups;
import com.example.monitoring_service.application.dto.HealthGroupOutcome;
import com.example.monitoring_service.application.dto.HealthGroupResult;
import com.example.monitoring_service.application.dto.RegisteredInstance;
import com.example.monitoring_service.application.dto.response.InstanceStatusResponse;
import com.example.monitoring_service.application.dto.response.SystemStatusResponse;
import com.example.monitoring_service.application.port.in.GetSystemStatusUseCase;
import com.example.monitoring_service.application.port.out.HealthGroupClientPort;
import com.example.monitoring_service.application.port.out.ServiceRegistryPort;
import com.example.monitoring_service.domain.model.HealthState;
import com.example.monitoring_service.domain.model.InstanceHealth;
import com.example.monitoring_service.domain.model.ServiceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
public class SystemStatusService implements GetSystemStatusUseCase {

    private final ServiceRegistryPort serviceRegistryPort;
    private final HealthGroupClientPort healthGroupClientPort;
    private final Executor healthCheckExecutor;
    private final Clock clock;
    private final List<String> expectedServices;

    @Override
    public SystemStatusResponse getSystemStatus() {
        // lấy list instances các service
        List<RegisteredInstance> instances = serviceRegistryPort.findAllInstances();

        // Gọi song song để thời gian phản hồi bằng instance chậm nhất thay vì cộng dồn.
        List<CompletableFuture<InstanceStatusResponse>> futures = instances.stream()
                .map(instance -> CompletableFuture.supplyAsync(() -> checkInstance(instance), healthCheckExecutor))
                .toList();

        List<InstanceStatusResponse> results = new ArrayList<>(futures.stream()
                .map(CompletableFuture::join)
                .toList());

        results.addAll(findMissingServices(instances));

        results.sort(Comparator.comparing(InstanceStatusResponse::serviceName)
                .thenComparing(InstanceStatusResponse::instanceId, Comparator.nullsFirst(Comparator.<String>naturalOrder())));

        long unhealthyCount = results.stream()
                .filter(result -> result.status() == ServiceStatus.DOWN
                        || result.status() == ServiceStatus.UNREACHABLE
                        || result.status() == ServiceStatus.MISSING)
                .count();
        log.info("Kiểm tra trạng thái hệ thống: instance_count={}, unhealthy_count={}", results.size(), unhealthyCount);

        return new SystemStatusResponse(OffsetDateTime.now(clock), results);
    }

    // Instance chết sẽ bị Eureka gỡ khỏi registry; đối chiếu danh sách mong đợi để UI vẫn thấy service bị thiếu.
    private List<InstanceStatusResponse> findMissingServices(List<RegisteredInstance> instances) {
        Set<String> registeredServices = instances.stream()
                .map(RegisteredInstance::serviceName)
                .collect(Collectors.toSet());
        List<InstanceStatusResponse> missing = expectedServices.stream()
                .map(serviceName -> serviceName.toLowerCase(Locale.ROOT))
                .distinct()
                .filter(serviceName -> !registeredServices.contains(serviceName))
                .map(serviceName -> new InstanceStatusResponse(serviceName, null, null, null,
                        ServiceStatus.MISSING, Map.of(), Map.of()))
                .toList();
        if (!missing.isEmpty()) {
            log.warn("Service mong đợi không có trong Eureka: services={}",
                    missing.stream().map(InstanceStatusResponse::serviceName).toList());
        }
        return missing;
    }
//    kiểm tra  heatl của một instance và trả về InstanceStatusResponse tương ứng
    private InstanceStatusResponse checkInstance(RegisteredInstance instance) {
        // Gọi health group optional ở thread khác để chạy song song với critical
        CompletableFuture<HealthGroupResult> optionalFuture = CompletableFuture.supplyAsync(
                        () -> healthGroupClientPort.fetchHealthGroup(instance.baseUrl(), HealthGroups.OPTIONAL),
                healthCheckExecutor);
        // Gọi health group bắt buộc ở thread hiện tại để chạy song song với OPTIONAL
        HealthGroupResult critical = healthGroupClientPort.fetchHealthGroup(instance.baseUrl(), HealthGroups.CRITICAL);
        // chờ
        HealthGroupResult optional = optionalFuture.join();

        InstanceHealth health = toInstanceHealth(instance, critical, optional);
        return new InstanceStatusResponse(
                instance.serviceName(),
                instance.instanceId(),
                instance.baseUrl(),
                instance.registryStatus(),
                health.resolveStatus(),
                new TreeMap<>(health.getCriticalComponents()),
                new TreeMap<>(health.getOptionalComponents()));
    }

    // tạo InstanceHealth thông tin instance và 2 gruops HealthGroupResult
    private InstanceHealth toInstanceHealth(RegisteredInstance instance, HealthGroupResult critical,
                                            HealthGroupResult optional) {
        if (critical.outcome() == HealthGroupOutcome.UNREACHABLE) {
            return InstanceHealth.unreachable();
        }
        if (critical.outcome() == HealthGroupOutcome.NOT_CONFIGURED) {
            log.warn("Instance chưa khai báo health group bắt buộc: service={}, instance_id={}",
                    instance.serviceName(), instance.instanceId());
            return InstanceHealth.reachable(HealthState.UNKNOWN, Map.of(), toStates(optional));
        }
        return InstanceHealth.reachable(HealthState.fromCode(critical.status()), toStates(critical), toStates(optional));
    }

    private Map<String, HealthState> toStates(HealthGroupResult result) {
        if (result.outcome() != HealthGroupOutcome.AVAILABLE) {
            return Map.of();
        }
        Map<String, HealthState> states = new TreeMap<>();
        result.components().forEach((name, status) -> states.put(name, HealthState.fromCode(status)));
        return states;
    }
}
