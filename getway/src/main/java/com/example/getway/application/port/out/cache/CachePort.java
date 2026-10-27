package com.example.getway.application.port.out.cache;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface CachePort {
    <T> Optional<T> get(String key, Class<T> valueType);

    <T> Optional<List<T>> getList(String key, Class<T> elementType);

    <T> void set(String key, T data, Duration ttl);

    <T> void set(String key, T data);

    <T> boolean setIfAbsent(String key, T data, Duration ttl);

    boolean exists(String key);

    void delete(String key);

    // Đọc và xóa phải atomic để một giá trị không được tiêu thụ hai lần.
    <T> Optional<T> getAndDelete(String key, Class<T> valueType);
}
