package com.example.producr_service.adapter.entity;

import com.example.producr_service.application.port.out.storage.StorageBucket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "image_url")
    private String imageUrl;

    // Vị trí ảnh trong storage để dọn file khi rollback hoặc xóa.
    @Column(name = "object_path", length = 500)
    private String objectPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_bucket", length = 50)
    private StorageBucket storageBucket;

    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
