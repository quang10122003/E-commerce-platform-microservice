package com.example.producr_service.adapter.out.persistence.FlashSaleCampaign;

import com.example.producr_service.adapter.entity.FlashSaleCampaignEntity;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlashSaleCampaignJpa extends JpaRepository<FlashSaleCampaignEntity, Long> {

    // Khóa chia sẻ theo khóa chính, phối hợp với khóa độc quyền của thao tác hủy.
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select c from FlashSaleCampaignEntity c where c.id = :campaignId")
    Optional<FlashSaleCampaignEntity> findByIdForShare(@Param("campaignId") Long campaignId);

    // Khóa độc quyền theo khóa chính; chờ các transaction mua giữ FOR SHARE hoàn tất.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from FlashSaleCampaignEntity c where c.id = :campaignId")
    Optional<FlashSaleCampaignEntity> findByIdForUpdate(@Param("campaignId") Long campaignId);

    // Lọc trước phân trang; ưu tiên trạng thái rồi khoảng cách thời gian cả trước và sau now.
    @Query(value = """
            select c.*
            from flash_sale_campaigns c
            where (:fromTime is null or c.starts_at >= :fromTime)
              and (:toTime is null or c.starts_at <= :toTime)
              and (:status is null or c.status = :status)
              and (:registrationOpen is null
                   or (:registrationOpen = true and c.status = 'DRAFT' and c.registration_deadline > :now)
                   or (:registrationOpen = false and (c.status <> 'DRAFT' or c.registration_deadline <= :now)))
            order by case c.status
                         when 'PUBLISHED' then 0
                         when 'DRAFT' then 1
                         when 'CANCELLED' then 2
                         when 'DONE' then 3
                         else 4
                     end,
                     abs(timestampdiff(MICROSECOND, :now, c.starts_at)),
                     c.starts_at asc,
                     c.id asc
            """,
            countQuery = """
            select count(*)
            from flash_sale_campaigns c
            where (:fromTime is null or c.starts_at >= :fromTime)
              and (:toTime is null or c.starts_at <= :toTime)
              and (:status is null or c.status = :status)
              and (:registrationOpen is null
                   or (:registrationOpen = true and c.status = 'DRAFT' and c.registration_deadline > :now)
                   or (:registrationOpen = false and (c.status <> 'DRAFT' or c.registration_deadline <= :now)))
            """,
            nativeQuery = true)
    Page<FlashSaleCampaignEntity> findCampaigns(
            @Param("fromTime") LocalDateTime from,
            @Param("toTime") LocalDateTime to,
            @Param("status") String status,
            @Param("now") LocalDateTime now,
            @Param("registrationOpen") Boolean registrationOpen,
            Pageable pageable
    );

    // Bỏ qua chương trình đã hủy hoặc hoàn thành và cho phép hai lịch tiếp giáp nhau.
    @Query("""
            select count(c) > 0
            from FlashSaleCampaignEntity c
            where c.status not in (:excludedStatuses)
              and c.startsAt < :endsAt
              and c.endsAt > :startsAt
            """)
    boolean existsOverlapping(
            @Param("startsAt") LocalDateTime startsAt,
            @Param("endsAt") LocalDateTime endsAt,
            @Param("excludedStatuses") Collection<FlashSaleCampaignStatus> excludedStatuses
    );
}
