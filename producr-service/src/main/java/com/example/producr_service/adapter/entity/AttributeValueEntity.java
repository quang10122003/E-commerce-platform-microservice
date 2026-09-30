package com.example.producr_service.adapter.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attribute_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK attributes_id - tro toi LOAI thuoc tinh, ON DELETE CASCADE o DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attributes_id", nullable = false)
    private AttributeEntity attribute;

    @Column(name = "value", nullable = false)
    private String value;

    // So sánh theo ID khi đã lưu, còn bản ghi mới chỉ bằng chính nó.
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AttributeValueEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    // Giữ hash ổn định khi JPA gán ID sau khi lưu.
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }


}
