package com.example.producr_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

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

    // Dựng lại chương trình đã lưu, không yêu cầu hạn đăng ký còn ở tương lai.
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
            LocalDateTime updatedAt) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID chương trình đã lưu phải lớn hơn 0");
        }
        return new FlashSaleCampaign(id, name, description, registrationDeadline,
                startsAt, endsAt, status, createdBy, createdAt, updatedAt);
    }
}
