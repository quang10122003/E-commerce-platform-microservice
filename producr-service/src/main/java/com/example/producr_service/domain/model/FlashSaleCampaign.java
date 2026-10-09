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

// chương trình sale
@Getter
public class FlashSaleCampaign {
    private final  Long id;
    private  String name;
    private  String description;
    private  LocalDateTime registrationDeadline;
    private  LocalDateTime startsAt;
    private  LocalDateTime endsAt;
    // Vòng đời chương trình; quyền đăng ký và mua còn phụ thuộc các mốc thời gian.
    private  FlashSaleCampaignStatus status;
    private  Long createdBy;
    private  LocalDateTime createdAt;
    private  LocalDateTime updatedAt;
    // Aggregate luôn chứa đầy đủ đăng ký; dữ liệu đọc danh sách dùng DTO riêng.
    private final List<FlashSaleItem> items = new ArrayList<>();

    // Không cho caller thay đổi collection ngoài các hàm nghiệp vụ.
    public List<FlashSaleItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    // Khởi tạo state hợp lệ dùng chung cho tạo mới và dựng lại dữ liệu.
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

    // Kiểm tra điều kiện và thêm đăng ký vào aggregate đã tải đầy đủ.
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

    // Shop xóa vật lý đăng ký của mình chỉ khi DRAFT và chưa hết hạn.
    public void removeItem(Long itemId, Long shopId, LocalDateTime now) {
        validateRegistration(now);
        FlashSaleItem item = items.stream()
                .filter(value -> itemId != null && itemId.equals(value.id()) && value.shopId().equals(shopId))
                .findFirst().orElseThrow(() -> new BusinessException(DomainProductError.FLASH_SALE_ITEM_NOT_FOUND));
        items.remove(item);
        updatedAt = now;
    }

    // Admin block chỉ đổi trạng thái, không xóa item hoặc số liệu lịch sử.
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

    // Chỉ nhận đăng ký khi còn DRAFT và chưa đến hạn đóng đăng ký.
    public void validateRegistration(LocalDateTime now) {
        if (!isRegistrationOpen(now)) {
            throw new BusinessException(DomainProductError.FLASH_SALE_REGISTRATION_CLOSED);
        }
    }

    // Tạo chương trình DRAFT để Shop đăng ký ngay, đến trước hạn đăng ký.
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
        // Giữ đủ thời gian chuẩn bị trước khi chương trình bắt đầu.
        if (startsAt.isBefore(now.plusHours(3))) {
            throw new BusinessException(DomainProductError.FLASH_SALE_START_TOO_SOON);
        }
        // Khóa đăng ký trước giờ chạy ít nhất 30 phút để chốt danh sách.
        if (registrationDeadline.isAfter(startsAt.minusMinutes(30))) {
            throw new BusinessException(DomainProductError.FLASH_SALE_REGISTRATION_GAP_TOO_SHORT);
        }
        return campaign;
    }

    // Shop chỉ được đăng ký khi chương trình còn DRAFT và chưa đến hạn.
    public boolean isRegistrationOpen(LocalDateTime now) {
        Objects.requireNonNull(now, "Thời điểm hiện tại không được null");
        return status == FlashSaleCampaignStatus.DRAFT && now.isBefore(registrationDeadline);
    }

    // Chỉ cho phép chuyển trạng thái theo vòng đời; không xử lý mốc thời gian.
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

    // Hủy chương trình chưa hoàn thành; trả true khi thay đổi, gọi lại giữ nguyên updatedAt.
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

    // Dựng aggregate đầy đủ, không yêu cầu hạn đăng ký còn ở tương lai.
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
        // Không nhận collection thiếu hoặc đăng ký thuộc chương trình khác.
        List<FlashSaleItem> restoredItems = List.copyOf(items);
        if (restoredItems.stream().anyMatch(item -> !id.equals(item.campaignId()))) {
            throw new IllegalArgumentException("Item không thuộc chương trình");
        }
        campaign.items.addAll(restoredItems);
        return campaign;
    }

}
