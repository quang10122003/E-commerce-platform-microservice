package com.example.getway.adapter.out.cache;

import com.example.common.exception.CacheAccessException;
import com.example.getway.application.port.out.cache.CachePort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> Optional<T> get(String key, Class<T> valueType) {
        return execute(() -> decode(stringRedisTemplate.opsForValue().get(key), valueType));
    }

    @Override
    public <T> Optional<List<T>> getList(String key, Class<T> elementType) {
        return execute(() -> {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            List<T> items = objectMapper.readValue(value,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
            if (items == null || items.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Dữ liệu cache không phải danh sách hợp lệ");
            }
            return Optional.of(List.copyOf(items));
        });
    }

    @Override
    public <T> void set(String key, T data, Duration ttl) {
        execute(() -> {
            stringRedisTemplate.opsForValue().set(key, encode(data), ttl);
            return null;
        });
    }

    @Override
    public <T> void set(String key, T data) {
        execute(() -> {
            stringRedisTemplate.opsForValue().set(key, encode(data));
            return null;
        });
    }

    @Override
    public <T> boolean setIfAbsent(String key, T data, Duration ttl) {
        return execute(() -> Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                .setIfAbsent(key, encode(data), ttl)));
    }

    @Override
    public boolean exists(String key) {
        return execute(() -> Boolean.TRUE.equals(stringRedisTemplate.hasKey(key)));
    }

    @Override
    public void delete(String key) {
        execute(() -> stringRedisTemplate.delete(key));
    }

    @Override
    public <T> Optional<T> getAndDelete(String key, Class<T> valueType) {
        return execute(() -> decode(stringRedisTemplate.opsForValue().getAndDelete(key), valueType));
    }

    private String encode(Object data) {
        Objects.requireNonNull(data, "Dữ liệu cache không được null");
        return data instanceof String text ? text : objectMapper.writeValueAsString(data);
    }

    private <T> Optional<T> decode(String value, Class<T> valueType) {
        if (value == null) {
            return Optional.empty();
        }
        T data = valueType == String.class ? valueType.cast(value) : objectMapper.readValue(value, valueType);
        if (data == null) {
            throw new IllegalArgumentException("Dữ liệu cache không được null");
        }
        return Optional.of(data);
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (DataAccessException exception) {
            throw new CacheAccessException("Không thể truy cập cache", exception, false);
        } catch (JacksonException | IllegalArgumentException exception) {
            throw new CacheAccessException("Không thể xử lý dữ liệu cache", exception, true);
        }
    }
}
