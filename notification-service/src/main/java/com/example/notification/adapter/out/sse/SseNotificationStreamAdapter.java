package com.example.notification.adapter.out.sse;

import com.example.notification.application.port.out.NotificationStreamPort;
import com.example.common.exception.BusinessException;
import com.example.notification.application.error.NotificationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class SseNotificationStreamAdapter implements NotificationStreamPort {
    // chú ý :Kết nối và replay chỉ nằm ở một instance; thêm Redis fan-out khi chạy nhiều instance.
    // map lưu các kết nối theo key
    private final ConcurrentMap<String, List<SseEmitter>> connections = new ConcurrentHashMap<>();
    // lưu cache event của từng key
    private final ConcurrentMap<String, Deque<CachedEvent>> eventHistory = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration replayTtl;
    private final long connectionTimeoutMs;
    private final int replayMaxEvents;

    public SseNotificationStreamAdapter(Clock clock,
            @Value("${app.sse.replay-ttl-ms}") long replayTtlMs,
            @Value("${app.sse.connection-timeout-ms}") long connectionTimeoutMs,
            @Value("${app.sse.replay-max-events}") int replayMaxEvents) {
        if (replayTtlMs <= 0 || connectionTimeoutMs <= 0 || replayMaxEvents <= 0) {
            throw new IllegalArgumentException("Thời gian kết nối, lưu event và giới hạn lịch sử SSE phải lớn hơn 0");
        }
        this.clock = clock;
        this.replayTtl = Duration.ofMillis(replayTtlMs);
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.replayMaxEvents = replayMaxEvents;
    }

    // Replay và publish dùng cùng khóa đồng bộ để event mới không chen vào giữa lịch sử đang phát lại.
    public synchronized SseEmitter connect(String key, UUID lastEventId) throws IOException {
        Objects.requireNonNull(key);
        // xóa các cache hết hạn , báo lỗi nếu mà event id cuối mà fe gửi lên đã bị xóa
        Deque<CachedEvent> history = eventHistory.get(key);
        if (history != null) {
            history.removeIf(event -> !event.expiresAt().isAfter(clock.instant()));
        }
        if (lastEventId != null && (history == null
                || history.stream().noneMatch(event -> event.eventId().equals(lastEventId)))) {
            throw new BusinessException(NotificationError.SSE_REPLAY_UNAVAILABLE);
        }
        // tạo kết nối với timeout
        SseEmitter emitter = new SseEmitter(connectionTimeoutMs);
        // đăng ký xóa két nối khi kết nối hoàn tất gửi dữ liêu
        emitter.onCompletion(() -> removeConnection(key, emitter));
        emitter.onError(exception -> removeConnection(key, emitter));
        // đăng ký khi kết nối timeout ( xóa kết nối kèm  kết thúc request HTTP)
        emitter.onTimeout(() -> {
            removeConnection(key, emitter);
            emitter.complete();
        });
        // thêm kết nối vào map quản lý theo key
        connections.compute(key, (connectionKey, emitters) -> {
            if (emitters == null) {
                emitters = new CopyOnWriteArrayList<>();
            }
            emitters.add(emitter);
            return emitters;
        });
        // thực hiện gửi test commet để test kết nối và hướng dẫn fe kết nối lại
        try {
            emitter.send(SseEmitter.event().comment("connected").reconnectTime(3000));
        } catch (IOException | RuntimeException exception) {
            removeConnection(key, emitter);
            throw exception;
        }

        // gửi lại lịch sử data theo key đc cache trên servẻr theo id fe gửi lên
        boolean replay = lastEventId == null;
        if (history != null) {
            for (CachedEvent event : history) {
                if (replay) {
                    send(key, emitter, SseEmitter.event().id(event.eventId().toString())
                            .name(event.eventType()).data(event.data(), MediaType.APPLICATION_JSON));
                } else if (event.eventId().equals(lastEventId)) {
                    replay = true;
                }
            }
        }
        return emitter;
    }

    @Override
    public synchronized void publish(String key, UUID eventId, String eventType, Object data) {
        if (eventType == null || !eventType.matches("[A-Za-z0-9_-]+") || "heartbeat".equals(eventType)) {
            throw new IllegalArgumentException("Tên event SSE không hợp lệ hoặc dành riêng cho heartbeat");
        }
        Objects.requireNonNull(key);
        Objects.requireNonNull(eventId);
        Objects.requireNonNull(data);
        Instant now = clock.instant();

        // lấy lịch sử
        Deque<CachedEvent> history = eventHistory.computeIfAbsent(key, historyKey -> new ArrayDeque<>());

        // xóa các event hết hạn
        history.removeIf(event -> !event.expiresAt().isAfter(now));
        if (history.stream().anyMatch(event -> event.eventId().equals(eventId))) {
            return;
        }
        // lưu cache lịch sử data và k có quá replayMaxEvents
        history.addLast(new CachedEvent(eventId, eventType, data, now.plus(replayTtl)));
        while (history.size() > replayMaxEvents) {
            history.removeFirst();
        }
        // lấy các kết nối và gửi data
        List<SseEmitter> emitters = connections.get(key);
        log.info("Phát SSE: key={}, event_id={}, event_type={}, connection_count={}",
                key, eventId, eventType, emitters == null ? 0 : emitters.size());
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                send(key, emitter, SseEmitter.event().id(eventId.toString()).name(eventType)
                        .data(data, MediaType.APPLICATION_JSON));
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.sse.heartbeat-interval-ms}")
    public synchronized void heartbeat() {
        connections.forEach((key, emitters) -> {
            for (SseEmitter emitter : emitters) {
                send(key, emitter, SseEmitter.event().comment("heartbeat"));
            }
        });
        Instant now = clock.instant();
        // cache hết hạn hạn và key rỗng
        eventHistory.values().forEach(history -> history.removeIf(event -> !event.expiresAt().isAfter(now)));
        eventHistory.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    // xóa conect cũ
    private synchronized void removeConnection(String key, SseEmitter emitter) {
        connections.computeIfPresent(key, (connectionKey, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }

    private void send(String key, SseEmitter emitter, SseEmitter.SseEventBuilder event) {
        try {
            emitter.send(event);
        } catch (IOException | RuntimeException exception) {
            // Client đóng kết nối không được khiến Kafka retry thông báo cho mọi client khác.
            removeConnection(key, emitter);
            log.debug("Đóng kết nối SSE không còn nhận dữ liệu: key={}", key, exception);
        }
    }

    private record CachedEvent(UUID eventId, String eventType, Object data, Instant expiresAt) {}
}
