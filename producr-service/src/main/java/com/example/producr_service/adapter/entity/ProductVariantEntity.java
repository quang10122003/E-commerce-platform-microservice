package com.example.producr_service.adapter.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

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

    // Database xóa phân loại qua ON DELETE CASCADE khi sản phẩm cha bị xóa.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "products_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ProductEntity product;

    @Column(name = "sku", nullable = false, unique = true)
    private String sku;

    @Column(name = "price", nullable = false, precision = 12)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VariantImageEntity> images = new ArrayList<>();

    @BatchSize(size = 50)
    @ManyToMany
    // Hai FK chỉ cascade xóa hàng trong bảng nối, không xóa entity thuộc tính dùng chung.
    @JoinTable(
            name = "variant_attribute_values",
            joinColumns = @JoinColumn(name = "product_variants_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "attribute_values_id", nullable = false),
            foreignKey = @ForeignKey(name = "variant_attribute_values_ibfk_1",
                    foreignKeyDefinition = "FOREIGN KEY (product_variants_id) REFERENCES product_variants(id) ON DELETE CASCADE"),
            inverseForeignKey = @ForeignKey(name = "variant_attribute_values_ibfk_2",
                    foreignKeyDefinition = "FOREIGN KEY (attribute_values_id) REFERENCES attribute_values(id) ON DELETE CASCADE")
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
