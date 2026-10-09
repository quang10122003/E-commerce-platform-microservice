package com.example.producr_service.adapter.entity;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "flash_sale_campaigns")
@Getter
@Setter
@NoArgsConstructor
public class FlashSaleCampaignEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tải theo lô; xóa item khỏi collection sẽ xóa bản ghi, Admin block vẫn giữ item.
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "campaign", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlashSaleItemEntity> items = new ArrayList<>();

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    // Lưu trạng thái vòng đời theo enum nghiệp vụ FlashSaleCampaignStatus.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlashSaleCampaignStatus status;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // DB tự cập nhật thời gian khi bản ghi thay đổi.
    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;
}
