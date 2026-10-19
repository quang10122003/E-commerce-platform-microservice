package com.example.producr_service.domain.model;

import java.math.BigDecimal;
import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class FlashSaleCampaign {
    private final  Long id;
    private  String name;
    private  String description;
    private  LocalDateTime registrationDeadline;
    private  LocalDateTime startsAt;
    private  LocalDateTime endsAt;
    // Trạng thái vòng đời chưa đủ quyết định quyền đăng ký hoặc mua; còn phải kiểm tra mốc thời gian.
    private  FlashSaleCampaignStatus status;
    private  Long createdBy;
    private  LocalDateTime createdAt;
    private  LocalDateTime updatedAt;
    // Aggregate phải chứa đủ đăng ký; dữ liệu đọc thiếu items dùng DTO riêng để không lưu nhầm.
    private final List<FlashSaleItem> items = new ArrayList<>();

    // Không cho sửa collection trực tiếp để không bỏ qua ràng buộc nghiệp vụ của aggregate.
    public List<FlashSaleItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    private FlashSaleCampaign(Long id, String name, String description,
                              LocalDateTime registrationDeadline, LocalDateTime startsAt,
                              LocalDateTime endsAt, FlashSaleCampaignStatus status,
                              Long createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (name == null || name.isBlank() || name.length() > 255) {
            throw new BusinessException(DomainProductError.FLASH_SALE_NAME_INVALID);
        }
        if (registrationDeadline == null || startsAt == null || endsAt == null
                || !registrationDeadline.isBefore(startsAt) || !startsAt.isBefore(endsAt)) {
            throw new BusinessException(DomainProductError.FLASH_SALE_TIME_INVALID);
        }
        if (createdBy == null || createdBy <= 0) {
            throw new BusinessException(DomainProductError.FLASH_SALE_CREATOR_INVALID);
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.registrationDeadline = registrationDeadline;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.status = Objects.requireNonNull(status, "Trạng thái chương trình không được null");
        this.createdBy = createdBy;
        this.createdAt = Objects.requireNonNull(createdAt, "Thời điểm tạo không được null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Thời điểm cập nhật không được null");
    }

    public FlashSaleItem addItem(FlashSaleItem.RegistrationVariant variant,
            BigDecimal discountPercent, int registeredQuantity, LocalDateTime now) {
        validateRegistration(now);
        if (items.stream().anyMatch(item -> Objects.equals(item.variantId(), variant.variantId()))) {
            throw new BusinessException(DomainProductError.FLASH_SALE_ITEM_ALREADY_EXISTS);
        }
        FlashSaleItem item = FlashSaleItem.register(id, variant, discountPercent, registeredQuantity, now);
        items.add(item);
        updatedAt = now;
        return item;
    }

    // Shop chỉ được xóa đăng ký khi còn DRAFT và chưa hết hạn, không can thiệp sale đã công bố.
    public void removeItem(Long itemId, Long shopId, LocalDateTime now) {
        validateRegistration(now);
        FlashSaleItem item = items.stream()
                .filter(value -> itemId != null && itemId.equals(value.id()) && value.shopId().equals(shopId))
                .findFirst().orElseThrow(() -> new BusinessException(DomainProductError.FLASH_SALE_ITEM_NOT_FOUND));
        items.remove(item);
        updatedAt = now;
    }

    // Admin block phải giữ item và số liệu lịch sử, không xóa vật lý đăng ký.
    public void disableItem(Long itemId, LocalDateTime now) {
        for (int index = 0; index < items.size(); index++) {
            FlashSaleItem item = items.get(index);
            if (itemId != null && itemId.equals(item.id())) {
                FlashSaleItem disabled = item.disable(now);
                if (disabled != item) {
                    items.set(index, disabled);
                    updatedAt = now;
                }
                return;
            }
        }
        throw new BusinessException(DomainProductError.FLASH_SALE_ITEM_NOT_FOUND);
    }

    public void validateRegistration(LocalDateTime now) {
        if (!isRegistrationOpen(now)) {
            throw new BusinessException(DomainProductError.FLASH_SALE_REGISTRATION_CLOSED);
        }
    }

    public static FlashSaleCampaign create(
            String name,
            String description,
            LocalDateTime registrationDeadline,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Long createdBy,
            LocalDateTime now) {
        Objects.requireNonNull(now, "Thời điểm hiện tại không được null");
        FlashSaleCampaign campaign = new FlashSaleCampaign(null, name, description,
                registrationDeadline, startsAt, endsAt, FlashSaleCampaignStatus.DRAFT,
                createdBy, now, now);
        if (!registrationDeadline.isAfter(now)) {
            throw new BusinessException(DomainProductError.FLASH_SALE_REGISTRATION_DEADLINE_PASSED);
        }
        // Khoảng đệm trước giờ bắt đầu dành cho việc chuẩn bị chương trình.
        if (startsAt.isBefore(now.plusHours(3))) {
            throw new BusinessException(DomainProductError.FLASH_SALE_START_TOO_SOON);
        }
        // Khóa đăng ký trước giờ chạy ít nhất 30 phút để chốt danh sách.
        if (registrationDeadline.isAfter(startsAt.minusMinutes(30))) {
            throw new BusinessException(DomainProductError.FLASH_SALE_REGISTRATION_GAP_TOO_SHORT);
        }
        return campaign;
    }

    public boolean isRegistrationOpen(LocalDateTime now) {
        Objects.requireNonNull(now, "Thời điểm hiện tại không được null");
        return status == FlashSaleCampaignStatus.DRAFT && now.isBefore(registrationDeadline);
    }

    // Chuyển trạng thái không tự kiểm tra thời gian; caller phải kiểm tra mốc thời gian trước.
    public void updateStatus(FlashSaleCampaignStatus nextStatus) {
        if (nextStatus == null) {
            throw new BusinessException(DomainProductError.FLASH_SALE_STATUS_REQUIRED);
        }
        boolean allowed = switch (status) {
            case DRAFT -> nextStatus == FlashSaleCampaignStatus.PUBLISHED
                    || nextStatus == FlashSaleCampaignStatus.CANCELLED;
            case PUBLISHED -> nextStatus == FlashSaleCampaignStatus.CANCELLED
                    || nextStatus == FlashSaleCampaignStatus.DONE;
            case CANCELLED, DONE -> false;
        };
        if (!allowed) {
            throw new BusinessException(DomainProductError.FLASH_SALE_STATUS_TRANSITION_INVALID);
        }
        status = nextStatus;
    }

    // Hủy lại không cập nhật updatedAt để thao tác lặp không tạo thay đổi giả.
    public boolean cancel(LocalDateTime now) {
        Objects.requireNonNull(now, "Thời điểm hiện tại không được null");
        if (status == FlashSaleCampaignStatus.CANCELLED) {
            return false;
        }
        if (status == FlashSaleCampaignStatus.DONE) {
            throw new BusinessException(DomainProductError.FLASH_SALE_CANNOT_CANCEL);
        }
        status = FlashSaleCampaignStatus.CANCELLED;
        updatedAt = now;
        return true;
    }

    // Khôi phục dữ liệu đã lưu không được yêu cầu hạn đăng ký còn ở tương lai.
    public static FlashSaleCampaign reconstitute(
            Long id,
            String name,
            String description,
            LocalDateTime registrationDeadline,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            FlashSaleCampaignStatus status,
            Long createdBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<FlashSaleItem> items) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID chương trình đã lưu phải lớn hơn 0");
        }
        FlashSaleCampaign campaign = new FlashSaleCampaign(id, name, description, registrationDeadline,
                startsAt, endsAt, status, createdBy, createdAt, updatedAt);
        List<FlashSaleItem> restoredItems = List.copyOf(items);
        if (restoredItems.stream().anyMatch(item -> !id.equals(item.campaignId()))) {
            throw new IllegalArgumentException("Item không thuộc chương trình");
        }
        campaign.items.addAll(restoredItems);
        return campaign;
    }

}
