package com.example.producr_service.adapter.out.cache;

import com.example.common.flashsale.FlashSaleSlotKeys;
import com.example.producr_service.application.port.out.cache.FlashSaleSlotPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisFlashSaleSlotAdapter implements FlashSaleSlotPort {

    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
              return redis.call('INCRBY', KEYS[1], ARGV[1])
            end
            return -1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void initializeIfAbsent(Long variantId, long remaining, Duration ttl) {
        redisTemplate.opsForValue().setIfAbsent(FlashSaleSlotKeys.remaining(variantId), String.valueOf(remaining), ttl);
    }

    @Override
    public void release(Long variantId, int quantity) {
        try {
            redisTemplate.execute(RELEASE_SCRIPT, List.of(FlashSaleSlotKeys.remaining(variantId)), String.valueOf(quantity));
        } catch (RuntimeException exception) {
            // Không làm hỏng việc hoàn kho trong DB vì Redis; suất bị thiếu được ghi log để đối soát.
            log.error("Không trả được suất flash sale trên Redis: variant_id={}, quantity={}, error={}",
                    variantId, quantity, exception.getMessage());
        }
    }
}
