package com.example.notification.application.constant;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class NotificationTime {
    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    private NotificationTime() {}

    // Timestamp trong DB thuộc múi giờ Việt Nam; thêm offset khi đưa ra API hoặc socket.
    public static OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        return value == null ? null : value.atZone(ZONE_ID).toOffsetDateTime();
    }
}
