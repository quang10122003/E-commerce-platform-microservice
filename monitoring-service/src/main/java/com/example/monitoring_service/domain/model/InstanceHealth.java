package com.example.monitoring_service.domain.model;

import java.util.Map;
//domain model chứa kết quả health check của một instance. Nó là bước trung gian giữa dữ liệu thô HealthState và kết quả cuối ServiceStatus.
public final class InstanceHealth {

    private final boolean reachable;
    private final HealthState criticalState;
    private final Map<String, HealthState> criticalComponents;
    private final Map<String, HealthState> optionalComponents;

    private InstanceHealth(boolean reachable, HealthState criticalState,
                           Map<String, HealthState> criticalComponents,
                           Map<String, HealthState> optionalComponents) {
        this.reachable = reachable;
        this.criticalState = criticalState;
        this.criticalComponents = Map.copyOf(criticalComponents);
        this.optionalComponents = Map.copyOf(optionalComponents);
    }
//    liên lạc được với instance, kể cả khi nó trả 404 hay báo DOWN. Có dữ liệu thì truyền vào để resolveStatus() tự quyết.
    public static InstanceHealth reachable(HealthState criticalState,
                                           Map<String, HealthState> criticalComponents,
                                           Map<String, HealthState> optionalComponents) {
        return new InstanceHealth(true, criticalState, criticalComponents, optionalComponents);
    }
//    không liên lạc được với instance. Không có dữ liệu nào nên map rỗng, state UNKNOWN. Kết quả optional bị bỏ qua.
    public static InstanceHealth unreachable() {
        return new InstanceHealth(false, HealthState.UNKNOWN, Map.of(), Map.of());
    }

//    quy tắc nghiệp vụ đổi từ HealthState -> ServiceStatus
    public ServiceStatus resolveStatus() {
        if (!reachable) {
            return ServiceStatus.UNREACHABLE;
        }
        // Khớp cách Eureka xử lý: mọi trạng thái khác UP của nhóm bắt buộc đều không nhận request.
        if (criticalState != HealthState.UP) {
            return ServiceStatus.DOWN;
        }
        boolean optionalFailed = optionalComponents.values().stream()
                .anyMatch(state -> state != HealthState.UP);
        return optionalFailed ? ServiceStatus.DEGRADED : ServiceStatus.UP;
    }

    public boolean isReachable() {
        return reachable;
    }

    public HealthState getCriticalState() {
        return criticalState;
    }

    public Map<String, HealthState> getCriticalComponents() {
        return criticalComponents;
    }

    public Map<String, HealthState> getOptionalComponents() {
        return optionalComponents;
    }
}
