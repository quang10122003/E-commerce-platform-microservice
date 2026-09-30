package com.example.producr_service.adapter.entity;

import com.example.producr_service.application.port.out.storage.StorageBucket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "variant_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VariantImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // FK product_variants_id - ON DELETE CASCADE o DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variants_id", nullable = false)
    private ProductVariantEntity variant;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "object_path", nullable = false, length = 500)
    private String objectPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_bucket", nullable = false, length = 50)
    private StorageBucket storageBucket;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

}
