package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

public interface FlashSaleCampaignRepositoryPort {

    // Kiểm tra lịch trùng, bỏ qua chương trình đã hủy hoặc hoàn thành.
    boolean existsOverlapping(LocalDateTime startsAt, LocalDateTime endsAt);

    // Lọc và sắp xếp theo nhóm trạng thái, độ gần thời điểm hiện tại trước khi phân trang.
    PageResponse<FlashSaleCampaign> findAll(PageQuery pageQuery, LocalDateTime from, LocalDateTime to,
                                        FlashSaleCampaignStatus status, LocalDateTime now);

    // Đọc và khóa độc quyền chương trình đến khi transaction kết thúc.
    Optional<FlashSaleCampaign> findByIdForUpdate(Long campaignId);

    // Lưu chương trình Flash Sale.
    void save(FlashSaleCampaign campaign);
}
