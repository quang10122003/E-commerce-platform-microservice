package com.example.order_service.application.dto.analytics;

import java.time.Duration;
import java.time.LocalDateTime;

// Trung bình số phút của một chặng xử lý; bỏ qua đơn chưa đi hết chặng.
public class DurationAverage {
    private long totalMinutes;
    private long count;

    public void add(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            totalMinutes += Duration.between(from, to).toMinutes();
            count++;
        }
    }

    public Long minutes() {
        return count == 0 ? null : totalMinutes / count;
    }
}
