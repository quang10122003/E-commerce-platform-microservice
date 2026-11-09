package com.example.monitoring_service.application.port.out;

import com.example.monitoring_service.application.dto.RegisteredInstance;

import java.util.List;

public interface ServiceRegistryPort {
    List<RegisteredInstance> findAllInstances();
}
