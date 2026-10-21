package com.example.producr_service.adapter.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

    // Database xóa giá trị thuộc tính qua ON DELETE CASCADE khi thuộc tính cha bị xóa.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attributes_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AttributeEntity attribute;

    @Column(name = "value", nullable = false)
    private String value;

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
