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
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlashSaleCampaignJpa extends JpaRepository<FlashSaleCampaignEntity, Long> {

    @Query(value = """
            select id from flash_sale_campaigns
            where (status = 'DRAFT' and starts_at <= :now)
               or (status = 'PUBLISHED' and ends_at <= :now)
            order by id asc
            """, nativeQuery = true)
    List<Long> findIdsDueForStatusTransition(@Param("now") LocalDateTime now);

    // Bỏ qua chương trình đang bị khóa để các instance job không chờ hoặc xử lý trùng nhau.
    @Query(value = """
            select * from flash_sale_campaigns where id = :id for update skip locked
            """, nativeQuery = true)
    Optional<FlashSaleCampaignEntity> findByIdForUpdateSkipLocked(@Param("id") Long id);

    // Khóa chia sẻ phải tồn tại trong transaction mua để thao tác hủy không chen ngang.
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select c from FlashSaleCampaignEntity c where c.id = :campaignId")
    Optional<FlashSaleCampaignEntity> findByIdForShare(@Param("campaignId") Long campaignId);

    // Thao tác hủy phải chờ các transaction mua đang giữ khóa chia sẻ hoàn tất.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from FlashSaleCampaignEntity c where c.id = :campaignId")
    Optional<FlashSaleCampaignEntity> findByIdForUpdate(@Param("campaignId") Long campaignId);

    // Khoảng cách thời gian xét cả lịch quá khứ và tương lai, không chỉ lịch sắp bắt đầu.
    @Query(value = """
            select c.*
            from flash_sale_campaigns c
            where (:fromTime is null or c.starts_at >= :fromTime)
              and (:toTime is null or c.starts_at <= :toTime)
              and (:status is null or c.status = :status)
              and (:registrationOpen is null
                   or (:registrationOpen = true and c.status = 'DRAFT' and c.registration_deadline > :now)
                   or (:registrationOpen = false and (c.status != 'DRAFT' or c.registration_deadline <= :now)))
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
                   or (:registrationOpen = false and (c.status != 'DRAFT' or c.registration_deadline <= :now)))
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

    // Hai lịch tiếp giáp không xung đột; chương trình đã hủy hoặc hoàn thành không chiếm lịch.
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
