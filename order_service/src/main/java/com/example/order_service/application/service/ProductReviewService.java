package com.example.order_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.dto.event.ProductReviewedPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.dto.response.ProductReviewPageResponse;
import com.example.order_service.application.dto.response.ProductReviewResponse;
import com.example.order_service.application.dto.request.CreateReviewRequest;
import com.example.order_service.application.dto.review.ReviewImageUpload;
import com.example.order_service.application.dto.review.ReviewableOrderItemView;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.in.CreateProductReviewUseCase;
import com.example.order_service.application.port.in.GetProductReviewsUseCase;
import com.example.order_service.application.port.out.client.CurrentUserPort;
import com.example.order_service.application.port.out.client.UserAvatarPort;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.port.out.repo.ProductReviewRepoPort;
import com.example.order_service.application.port.out.storage.FileStoragePort;
import com.example.order_service.application.port.out.storage.StorageBucket;
import com.example.order_service.application.port.out.storage.StoredFile;
import com.example.order_service.application.port.out.storage.StoredFileRollbackPort;
import com.example.order_service.domain.model.ProductReview;
import com.example.order_service.domain.model.RatingSummary;
import com.example.order_service.domain.model.ReviewImage;
import com.example.order_service.domain.model.ReviewImageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Slf4j
@RequiredArgsConstructor
public class ProductReviewService implements CreateProductReviewUseCase, GetProductReviewsUseCase {
    private final CurrentUserPort currentUserPort;
    private final UserAvatarPort userAvatarPort;
    private final ProductReviewRepoPort reviewRepoPort;
    private final FileStoragePort fileStoragePort;
    private final StoredFileRollbackPort storedFileRollbackPort;
    private final OutboxPort outboxPort;
    private final Executor ratingSummaryExecutor;
    private final Clock clock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductReviewResponse createReview(CreateReviewRequest request, List<ReviewImageUpload> uploads) {
        Long userId = currentUserPort.getInfo().userId();
        // Món của người khác trả như không tồn tại để không lộ đơn hàng.
        ReviewableOrderItemView item = reviewRepoPort.findReviewableOrderItem(request.orderItemId())
                .filter((ReviewableOrderItemView view) -> userId.equals(view.buyerId()))
                .orElseThrow(() -> new BusinessException(OrderError.REVIEW_ORDER_ITEM_NOT_FOUND));

        if (reviewRepoPort.existsByOrderItemId(item.orderItemId())) {
            throw new BusinessException(OrderError.REVIEW_ALREADY_EXISTS);
        }
        List<ReviewImageUpload> images = uploads == null ? List.of() : uploads;
        LocalDateTime now = LocalDateTime.now(clock);
        ProductReview review = ProductReview.create(item.orderItemId(), item.orderId(), item.productId(),
                item.shopId(), userId, item.buyerName(), item.variantAttributes(), item.orderStatus(),
                item.deliveredAt(), request.rating(), request.content(), images.size(), now);
        // Kiểm tra mọi ảnh trước khi upload để request sai không để lại file trên Storage.
        List<ReviewImageFormat> formats = images.stream()
                .map((ReviewImageUpload image) -> ReviewImageFormat.detect(image.contentType(), image.content()))
                .toList();

        // Đọc điểm hiện có song song với upload ảnh để giảm thời gian phản hồi; đánh giá mới chưa ghi nên không bị tính trùng.
        CompletableFuture<RatingSummary> summary = item.productId() == null ? null
                : CompletableFuture.supplyAsync(() -> reviewRepoPort.findRatingSummary(item.productId()),
                ratingSummaryExecutor);
        uploadImages(review, userId, images, formats);
        ProductReview saved = reviewRepoPort.save(review);
        // Sản phẩm đã bị xóa thì không còn điểm để cập nhật bên Product Service.
        if (summary != null) {
            BigDecimal average = await(summary).add(saved.getRating()).average();
            ProductReviewedPayload event = new ProductReviewedPayload(UUID.randomUUID(),
                    ProductReviewedPayload.EVENT_TYPE, saved.getProductId(), average,
                    now.atZone(clock.getZone()).toOffsetDateTime());
            outboxPort.save(new OutboxEventDto(event.eventId(), Constant.REVIEW, saved.getId().toString(),
                    event.eventType(), event, now));
        }
        log.info("Người mua đã đánh giá sản phẩm: review_id={}, order_item_id={}, product_id={}, rating={}, images={}",
                saved.getId(), saved.getOrderItemId(), saved.getProductId(), saved.getRating(), saved.getImages().size());
        return toResponse(saved);
    }

    // Lấy tổng quan số sao (toàn bộ đánh giá) và một trang đánh giá mới nhất, có thể lọc theo một mức sao.
    @Override
    @Transactional(readOnly = true)
    public ProductReviewPageResponse getProductReviews(Long productId, Integer rating, PageQuery pageQuery) {
        Map<Integer, Long> counts = reviewRepoPort.countByRating(productId);
        long reviewCount = counts.values().stream().mapToLong(Long::longValue).sum();
        long ratingSum = counts.entrySet().stream().mapToLong((Map.Entry<Integer, Long> entry) -> entry.getKey() * entry.getValue()).sum();
        List<ProductReviewPageResponse.RatingCount> ratingCounts = IntStream.iterate(ProductReview.MAX_RATING,
                        (int stars) -> stars >= ProductReview.MIN_RATING, (int stars) -> stars - 1)
                .mapToObj((int stars) -> new ProductReviewPageResponse.RatingCount(stars, counts.getOrDefault(stars, 0L)))
                .toList();
        // Số đánh giá khớp bộ lọc lấy luôn từ số đếm theo mức sao, không cần đếm lại.
        long matched = rating == null ? reviewCount : counts.getOrDefault(rating, 0L);
        List<ProductReview> reviews = matched == 0 ? List.of()
                : reviewRepoPort.findProductReviews(productId, rating, pageQuery.pageIndex() * pageQuery.size(), pageQuery.size());
        // Một lần gọi theo lô cho cả trang; đánh giá của tài khoản đã xóa (user_id null) không có avatar.
        Map<Long, String> avatars = userAvatarPort.findAvatarUrls(reviews.stream()
                .map(ProductReview::getUserId).filter(Objects::nonNull).distinct().toList());
        List<ProductReviewPageResponse.Item> items = reviews.stream()
                .map((ProductReview review) -> toItem(review, avatars.get(review.getUserId()))).toList();
        int totalPages = (int) Math.ceil((double) matched / pageQuery.size());
        return new ProductReviewPageResponse(
                new ProductReviewPageResponse.Summary(new RatingSummary(reviewCount, ratingSum).average(), reviewCount, ratingCounts),
                new PageResponse<>(items, pageQuery.page(), pageQuery.size(), matched, totalPages));
    }

    private ProductReviewPageResponse.Item toItem(ProductReview review, String avatarUrl) {
        return new ProductReviewPageResponse.Item(review.getId(), review.getReviewerNameSnapshot(), avatarUrl, review.getRating(),
                review.getContent(), review.getVariantAttributes(),
                review.getImages().stream().map(ReviewImage::getImageUrl).toList(),
                review.getCreatedAt().atZone(clock.getZone()).toOffsetDateTime());
    }

    // Upload từng ảnh lên bucket "reviews" theo đường dẫn {userId}/{uuid}.{đuôi} rồi gắn URL vào đánh giá theo đúng thứ tự gửi lên.
    private void uploadImages(ProductReview review, Long userId, List<ReviewImageUpload> images,
                              List<ReviewImageFormat> formats) {
        List<StoredFile> storedFiles = new ArrayList<>();
        // Đăng ký trước khi upload để ảnh được dọn nếu transaction rollback, kể cả lỗi lúc commit.
        storedFileRollbackPort.deleteImageStore(storedFiles);
        for (int index = 0; index < images.size(); index++) {
            ReviewImageFormat format = formats.get(index);
            String objectPath = userId + "/" + UUID.randomUUID() + format.getExtension();
            String publicUrl = fileStoragePort.upload(StorageBucket.REVIEWS, objectPath, format.getContentType(),
                    images.get(index).content());
            storedFiles.add(new StoredFile(StorageBucket.REVIEWS, objectPath));
            review.addImage(new ReviewImage(null, publicUrl, objectPath, StorageBucket.REVIEWS.getBucketName(), index));
        }
    }

    // Chờ luồng đọc điểm đánh giá chạy xong và trả kết quả; lỗi ở luồng đó được ném lại nguyên gốc (bỏ lớp CompletionException) để xử lý như lỗi thường.
    private static RatingSummary await(CompletableFuture<RatingSummary> summary) {
        try {
            return summary.join();
        } catch (CompletionException exception) {
            if (exception.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw exception;
        }
    }

    private ProductReviewResponse toResponse(ProductReview review) {
        return new ProductReviewResponse(review.getId(), review.getOrderItemId(), review.getProductId(),
                review.getRating(), review.getContent(), review.getVariantAttributes(),
                review.getImages().stream().map(ReviewImage::getImageUrl).toList(),
                review.getCreatedAt().atZone(clock.getZone()).toOffsetDateTime());
    }
}
