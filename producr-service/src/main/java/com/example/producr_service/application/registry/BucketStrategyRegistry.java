package com.example.producr_service.application.registry;

import com.example.producr_service.application.strategy.interfaces.IBucketUploadStrategy;
import com.example.producr_service.application.port.out.storage.StorageBucket;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
public class BucketStrategyRegistry {
    private final Map<StorageBucket, IBucketUploadStrategy> strategies ;

    public BucketStrategyRegistry(List<IBucketUploadStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toUnmodifiableMap(IBucketUploadStrategy::supportedBucket,Function.identity()
                ));
    }

    public IBucketUploadStrategy get(StorageBucket storageBucket){
        IBucketUploadStrategy strategy = strategies.get(storageBucket);
        if(strategy == null){
            throw new IllegalArgumentException(
                    "Chưa đăng ký strategy cho bucket: " + storageBucket
            );
        }
        return strategy;
    }
}

