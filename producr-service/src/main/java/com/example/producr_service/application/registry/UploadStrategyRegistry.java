package com.example.producr_service.application.registry;

import com.example.producr_service.application.strategy.UploadPurpose;
import com.example.producr_service.application.strategy.interfaces.IUploadStrategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UploadStrategyRegistry {
    private final Map<UploadPurpose, IUploadStrategy> strategies;

    public UploadStrategyRegistry(List<IUploadStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(IUploadStrategy::supportedPurpose, Function.identity()));
    }

    public IUploadStrategy get(UploadPurpose uploadPurpose) {
        IUploadStrategy strategy = strategies.get(uploadPurpose);
        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Chưa đăng ký strategy cho mục đích upload: " + uploadPurpose
            );
        }
        return strategy;
    }
}
