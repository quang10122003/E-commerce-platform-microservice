package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.application.dto.response.FlashSaleCampaignView;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

public interface FlashSaleCampaignRepositoryPort {

    Optional<FlashSaleCampaignView> findViewById(Long campaignId);

    boolean existsOverlapping(LocalDateTime startsAt, LocalDateTime endsAt);

    PageResponse<FlashSaleCampaignView> findAll(PageQuery pageQuery, LocalDateTime from, LocalDateTime to,
                                        FlashSaleCampaignStatus status, LocalDateTime now, Boolean registrationOpen);

    Optional<FlashSaleCampaign> findByIdForUpdate(Long campaignId);

    // Tải aggregate đầy đủ và giữ khóa chia sẻ để ngăn hủy trong transaction mua.
    Optional<FlashSaleCampaign> findByIdForShare(Long campaignId);

    // Không dùng dữ liệu đọc thiếu items để lưu aggregate vì có thể xóa đăng ký hiện có.
    void save(FlashSaleCampaign campaign);

    List<Long> findIdsDueForStatusTransition(LocalDateTime now);

    Optional<FlashSaleCampaign> findStatusByIdForSkip(Long id);

    // Không đồng bộ collection khi job chỉ tải phần trạng thái, tránh xóa item chưa được tải.
    void saveStatus(Long id, FlashSaleCampaignStatus status);
}
