package com.example.producr_service.application.service;

import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.event.ProductReviewedPayload;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.outbox.ProductUpdatedOutboxPayload;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.port.out.repo.InboxEventPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.domain.model.InboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ProductReviewEventService {
    private final InboxEventPort inbox;
    private final ProductRepositoryPort productRepositoryPort;
    private final OutboxPort outbox;
    private final MapJsonToObjPort json;
    private final Clock clock;

    // Inbox commit cùng cập nhật điểm nên nhận lại cùng event không cộng số đánh giá lần nữa.
    @Transactional
    public void applyProductReviewed(ProductReviewedPayload event) {
        if (event == null || event.eventId() == null || !ProductReviewedPayload.EVENT_TYPE.equals(event.eventType())
                || event.productId() == null || event.productId() <= 0 || event.ratingAverage() == null
                || event.ratingAverage().compareTo(BigDecimal.ONE) < 0
                || event.ratingAverage().compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("Event đánh giá sản phẩm thiếu dữ liệu hợp lệ");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!inbox.tryInsert(InboxEvent.create(event.eventId(), event.eventType(), json.writeValue(event), now))) {
            log.info("Bỏ qua event đã xử lý: event_id={}, event_type={}", event.eventId(), event.eventType());
            return;
        }
        // Sản phẩm bị xóa sau khi người mua đánh giá thì không còn gì để cập nhật.
        if (productRepositoryPort.applyReview(event.productId(), event.ratingAverage()) != 1) {
            log.warn("Bỏ qua cập nhật điểm vì sản phẩm không còn: event_id={}, product_id={}",
                    event.eventId(), event.productId());
            return;
        }
        outbox.save(new OutboxEventDto(UUID.randomUUID(), Constant.PRODUCT, event.productId().toString(),
                ProductUpdatedOutboxPayload.EVENT_TYPE, new ProductUpdatedOutboxPayload(event.productId()), now));
        log.info("Đã cập nhật điểm đánh giá: event_id={}, product_id={}, rating_average={}",
                event.eventId(), event.productId(), event.ratingAverage());
    }
}
