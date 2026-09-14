package com.example.producr_service.adapter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProductVariantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // FK products_id - ON DELETE CASCADE o DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "products_id", nullable = false)
    private ProductEntity product;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "price", nullable = false, precision = 12)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VariantImageEntity> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "variant_attribute_values",
            joinColumns = @JoinColumn(name = "product_variants_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_values_id")
    )
    @Builder.Default
    private Set<AttributeValueEntity> attributeValues = new HashSet<>();


    public void addImage(VariantImageEntity image) {
        images.add(image);
        image.setVariant(this);
    }

    public void linkAttributeValue(AttributeValueEntity value) {
        attributeValues.add(value);
    }
}
