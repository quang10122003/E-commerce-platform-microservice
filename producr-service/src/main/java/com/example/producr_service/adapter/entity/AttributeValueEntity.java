package com.example.producr_service.adapter.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attribute_values")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AttributeValueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // FK attributes_id - tro toi LOAI thuoc tinh, ON DELETE CASCADE o DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attributes_id", nullable = false)
    private AttributeEntity attribute;

    @Column(name = "value", nullable = false)
    private String value;


}
