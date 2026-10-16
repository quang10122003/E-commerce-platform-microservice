package com.example.notification.adapter.out.cache;

import com.example.common.exception.BusinessException;
import com.example.notification.adapter.out.cache.key.NotificationCacheKeys;
import com.example.notification.application.dto.WsTicketData;
import com.example.notification.application.port.out.WsTicketStorePort;
import com.example.notification.domain.error.DomainWebSocketError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisWsTicketStoreAdapter implements WsTicketStorePort {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // SET NX kèm TTL không ghi đè vé đã cấp cho kết nối khác.
    @Override
    public boolean saveWsTicketIfAbsent(String ticket, WsTicketData data, Duration ttl) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(
                    NotificationCacheKeys.createWebSocketTicketKey(ticket), objectMapper.writeValueAsString(data), ttl));
        } catch (DataAccessException | JacksonException exception) {
            throw createTicketStoreUnavailableException(exception);
        }
    }

    // Chỉ một instance lấy được dữ liệu vé; các lần dùng lại nhận kết quả rỗng.
    @Override
    public Optional<WsTicketData> getAndDeleteWsTicket(String ticket) {
        try {
            String value = stringRedisTemplate.opsForValue().getAndDelete(
                    NotificationCacheKeys.createWebSocketTicketKey(ticket));
            return value == null ? Optional.empty() : Optional.of(objectMapper.readValue(value, WsTicketData.class));
        } catch (DataAccessException | JacksonException exception) {
            throw createTicketStoreUnavailableException(exception);
        }
    }

    // Fail closed như adapter Redis của gateway, không log vé hoặc nội dung xác thực.
    private BusinessException createTicketStoreUnavailableException(Exception exception) {
        log.error("Kho vé WebSocket không khả dụng: type={}", exception.getClass().getSimpleName());
        return new BusinessException(DomainWebSocketError.WS_TICKET_UNAVAILABLE);
    }
}
