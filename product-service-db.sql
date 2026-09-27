-- ============================================================
-- PRODUCT SERVICE - DATABASE SCHEMA (MySQL)
-- Mô hình EAV cho phân loại sản phẩm (màu, size...)
-- Quy ước khóa phụ: <tên_bảng_được_trỏ_tới>_id
-- ============================================================

CREATE DATABASE IF NOT EXISTS product_service_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE product_service_db;
-- Danh mục sản phẩm - phẳng, không phân cấp
CREATE TABLE categories (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    image_url VARCHAR(500) NULL,
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(36) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    max_retry INT NOT NULL DEFAULT 5,
    created_at DATETIME NOT NULL,
    next_attempt_at DATETIME NOT NULL,
    published_at DATETIME NULL,
    error_message TEXT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

-- Hãng sản phẩm - tách riêng để lọc/filter khi tìm kiếm
CREATE TABLE brands (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Sản phẩm gốc - KHÔNG chứa giá/tồn kho (nằm ở product_variants);
CREATE TABLE products (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    categories_id           INT NOT NULL,
    brands_id               INT NULL,   -- NULL nếu không rõ hãng

    name                    VARCHAR(255) NOT NULL,
    description             TEXT,
    image_url               VARCHAR(500) not null,  -- ảnh đại diện cho thẻ card, khác ảnh riêng ở variant_images
    object_path             VARCHAR(500) NULL,
    storage_bucket          VARCHAR(50) NULL,
    total_sold              BIGINT NOT NULL DEFAULT 0,
    is_active               ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',  -- shop ẩn/hiện sản phẩm

    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (categories_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (brands_id) REFERENCES brands(id) ON DELETE SET NULL
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- Loại thuộc tính của 1 sản phẩm (VD: "Màu sắc", "Size")
-- Sản phẩm không phân loại thì không có dòng nào ở đây
CREATE TABLE attributes (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    products_id             INT NOT NULL,
    name                    VARCHAR(100) NOT NULL,

    FOREIGN KEY (products_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- Giá trị cụ thể của thuộc tính (VD: "Đỏ", "Xanh", "S", "M")
CREATE TABLE attribute_values (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    attributes_id           INT NOT NULL,  -- trỏ tới LOẠI thuộc tính, không trỏ thẳng tới products
    value                   VARCHAR(100) NOT NULL,

    FOREIGN KEY (attributes_id) REFERENCES attributes(id) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- Biến thể cụ thể để bán - mỗi dòng là 1 tổ hợp mua được, chứa giá + tồn kho
-- order_items ở Order Service chỉ tham chiếu tới id của bảng này
CREATE TABLE product_variants (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    products_id             INT NOT NULL,
    sku                     VARCHAR(50) NOT NULL UNIQUE,
    price                   DECIMAL(12) NOT NULL,
    stock_quantity          INT NOT NULL DEFAULT 0,
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (products_id) REFERENCES products(id) ON DELETE CASCADE,
    CHECK (price >= 0),
    CHECK (stock_quantity >= 0)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- Ảnh riêng của từng variant - 1 variant có thể có nhiều ảnh (gallery)
CREATE TABLE variant_images (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    product_variants_id     INT NOT NULL,
    image_url               VARCHAR(500) NOT NULL,
    object_path             VARCHAR(500) NULL,
    storage_bucket          VARCHAR(50) NULL,
    is_primary              BOOLEAN NOT NULL DEFAULT FALSE,  -- ảnh chính, hiện trước tiên

    FOREIGN KEY (product_variants_id) REFERENCES product_variants(id) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Bảng nối nhiều-nhiều: 1 variant được tạo bởi nhiều giá trị thuộc tính
-- VD: (101, 1[Đỏ]) + (101, 4[M]) => variant 101 = Áo đỏ size M
-- Sản phẩm không phân loại thì variant không có dòng nào ở đây
CREATE TABLE variant_attribute_values (
    product_variants_id     INT NOT NULL,
    attribute_values_id     INT NOT NULL,

    PRIMARY KEY (product_variants_id, attribute_values_id),
    FOREIGN KEY (product_variants_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    FOREIGN KEY (attribute_values_id) REFERENCES attribute_values(id) ON DELETE CASCADE
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- ============================================================
-- INDEX
-- ============================================================
CREATE UNIQUE INDEX uk_outbox_event_id ON outbox_event(event_id);
CREATE INDEX idx_outbox_pending ON outbox_event(status, next_attempt_at, created_at);
CREATE INDEX idx_outbox_aggregate ON outbox_event(aggregate_type, aggregate_id, event_type);
CREATE INDEX idx_products_by_brand ON products(brands_id);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_variants_by_product ON product_variants(products_id);
CREATE INDEX idx_attributes_by_product ON attributes(products_id);
CREATE INDEX idx_values_by_attribute ON attribute_values(attributes_id);
CREATE INDEX idx_images_by_variant ON variant_images(product_variants_id);
CREATE INDEX idx_link_by_value ON variant_attribute_values(attribute_values_id);
