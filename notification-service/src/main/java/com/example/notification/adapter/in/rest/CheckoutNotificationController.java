package com.example.notification.adapter.in.rest;

import com.example.notification.adapter.out.sse.SseNotificationStreamAdapter;
import com.example.notification.application.port.in.CheckCheckoutNotificationAccessUseCase;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/checkouts")
@RequiredArgsConstructor
public class CheckoutNotificationController {
    private final CheckCheckoutNotificationAccessUseCase checkCheckoutNotificationAccessUseCase;
    private final SseNotificationStreamAdapter sseNotificationStreamAdapter;

    // api đăng ký sse từ fe
    @GetMapping(value = "/{checkoutId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@PathVariable @Positive Long checkoutId,
            @RequestHeader(value = "Last-Event-ID", required = false) UUID lastEventId) throws IOException {
        // check quyền đang ký
        checkCheckoutNotificationAccessUseCase.checkAccess(checkoutId);

        SseEmitter emitter = sseNotificationStreamAdapter.connect("checkout:" + checkoutId, lastEventId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header("X-Accel-Buffering", "no").body(emitter);
    }
}
