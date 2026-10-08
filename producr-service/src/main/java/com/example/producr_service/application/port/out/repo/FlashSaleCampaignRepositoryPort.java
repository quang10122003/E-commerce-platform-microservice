package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.application.dto.response.FlashSaleCampaignView;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

public interface FlashSaleCampaignRepositoryPort {

    // Kiểm tra lịch trùng, bỏ qua chương trình đã hủy hoặc hoàn thành.
    boolean existsOverlapping(LocalDateTime startsAt, LocalDateTime endsAt);

    // Lọc và sắp xếp theo nhóm trạng thái, độ gần thời điểm hiện tại trước khi phân trang.
    PageResponse<FlashSaleCampaignView> findAll(PageQuery pageQuery, LocalDateTime from, LocalDateTime to,
                                        FlashSaleCampaignStatus status, LocalDateTime now, Boolean registrationOpen);

    // Khóa chương trình trước khi tải aggregate đầy đủ để cập nhật an toàn.
    Optional<FlashSaleCampaign> findByIdForUpdate(Long campaignId);

    // Tải aggregate đầy đủ và giữ khóa chia sẻ để ngăn hủy trong transaction mua.
    Optional<FlashSaleCampaign> findByIdForShare(Long campaignId);

    // Lưu aggregate đầy đủ; dữ liệu đọc danh sách không dùng port này.
    void save(FlashSaleCampaign campaign);
}
