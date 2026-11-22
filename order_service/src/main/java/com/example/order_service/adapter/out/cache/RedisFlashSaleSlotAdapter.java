package com.example.order_service.adapter.out.cache;

import com.example.common.flashsale.FlashSaleSlotKeys;
import com.example.order_service.application.port.out.flashsale.FlashSaleSlotPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisFlashSaleSlotAdapter implements FlashSaleSlotPort {

    // Đủ dài hơn thời gian chờ thanh toán VNPay để checkout bị hủy muộn vẫn trả được suất.
    private static final Duration RESERVATION_TTL = Duration.ofDays(1);

    // Kiểm tra đủ suất cho mọi dòng rồi mới trừ để không trừ dở dang; marker chống trừ hai lần khi request bị gửi lại.
    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>("""
            local reservation = KEYS[#KEYS]
            if redis.call('EXISTS', reservation) == 1 then return 1 end
            local count = #KEYS - 1
            for i = 1, count do
              local remaining = redis.call('GET', KEYS[i])
              if remaining and tonumber(remaining) < tonumber(ARGV[i]) then return 0 end
            end
            local held = {}
            for i = 1, count do
              if redis.call('EXISTS', KEYS[i]) == 1 then
                redis.call('DECRBY', KEYS[i], ARGV[i])
                table.insert(held, KEYS[i])
                table.insert(held, ARGV[i])
              end
            end
            if #held > 0 then
              redis.call('HSET', reservation, unpack(held))
              redis.call('EXPIRE', reservation, ARGV[count + 1])
            end
            return 1
            """, Long.class);

    // Bộ đếm đã hết hạn cùng chiến dịch thì không tạo lại, tránh để lại key suất của sale đã kết thúc.
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>("""
            local held = redis.call('HGETALL', KEYS[1])
            if #held == 0 then return 0 end
            for i = 1, #held, 2 do
              if redis.call('EXISTS', held[i]) == 1 then
                redis.call('INCRBY', held[i], held[i + 1])
              end
            end
            redis.call('DEL', KEYS[1])
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean reserve(String checkoutKey, Map<Long, Integer> quantitiesByVariant) {
        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();
        quantitiesByVariant.forEach((Long variantId, Integer quantity) -> {
            keys.add(FlashSaleSlotKeys.remaining(variantId));
            args.add(String.valueOf(quantity));
        });
        keys.add(FlashSaleSlotKeys.reservation(checkoutKey));
        args.add(String.valueOf(RESERVATION_TTL.toSeconds()));
        try {
            return Long.valueOf(1).equals(redisTemplate.execute(RESERVE_SCRIPT, keys, args.toArray()));
        } catch (RuntimeException exception) {
            // Redis chỉ là bộ lọc phía trước; lỗi Redis không được chặn người mua, saga vẫn khóa DB để chống bán vượt.
            log.warn("Không giữ được suất flash sale trên Redis, chuyển cho saga kiểm tra: error={}", exception.getMessage());
            return true;
        }
    }

    @Override
    public void release(String checkoutKey) {
        try {
            redisTemplate.execute(RELEASE_SCRIPT, List.of(FlashSaleSlotKeys.reservation(checkoutKey)));
        } catch (RuntimeException exception) {
            // Không làm hỏng việc hủy checkout vì Redis; suất bị thiếu sẽ được ghi log để đối soát.
            log.error("Không trả được suất flash sale trên Redis: checkout_key={}, error={}", checkoutKey, exception.getMessage());
        }
    }
}
