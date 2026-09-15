package com.example.producr_service.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // FK categories_id - NOT NULL, ON DELETE RESTRICT o DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categories_id", nullable = false)
    private CategoryEntity category;

    // FK brands_id - NULL duoc phep, ON DELETE SET NULL o DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brands_id")
    private BrandEntity brand;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ON DELETE CASCADE o DB -> cascade = ALL, orphanRemoval = true o JPA
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeEntity> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantEntity> variants = new ArrayList<>();


    /** Helper giu 2 chieu quan he dong bo - tranh loi hay gap khi them AttributeEntity con thieu setProduct. */
    public void addAttribute(AttributeEntity attribute) {
        attributes.add(attribute);
        attribute.setProduct(this);
    }

    public void addVariant(ProductVariantEntity variant) {
        variants.add(variant);
        variant.setProduct(this);
    }
}
