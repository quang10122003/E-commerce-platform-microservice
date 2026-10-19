package com.example.producr_service.adapter.entity;

import com.example.producr_service.domain.model.FlashSaleItemStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flash_sale_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_flash_sale_campaign_variant",
                columnNames = {"flash_sale_campaigns_id", "product_variants_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class FlashSaleItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Xóa đăng ký không được cascade ngược sang chương trình sở hữu.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flash_sale_campaigns_id", nullable = false)
    private FlashSaleCampaignEntity campaign;

    // Liên kết có thể null khi variant bị xóa; snapshot vẫn được giữ.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variants_id")
    private ProductVariantEntity variant;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "variant_attributes", nullable = false, columnDefinition = "TEXT")
    private String variantAttributes;

    @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "registered_quantity", nullable = false)
    private int registeredQuantity;

    @Column(name = "sold_quantity", nullable = false)
    private long soldQuantity;

    // Trạng thái đăng ký của item, không phải trạng thái chương trình.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlashSaleItemStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
