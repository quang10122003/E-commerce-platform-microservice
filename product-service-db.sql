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
    image_url               VARCHAR(500) NOT NULL,  -- ảnh đại diện cho thẻ card, khác ảnh riêng ở variant_images
    object_path             VARCHAR(500) NOT NULL,
    storage_bucket          VARCHAR(50) NOT NULL,
    total_sold              BIGINT NOT NULL DEFAULT 0,
    is_active               ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',  -- shop ẩn/hiện sản phẩm

    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (categories_id) REFERENCES categories(id) ON DELETE RESTRICT,
    FOREIGN KEY (brands_id) REFERENCES brands(id) ON DELETE SET NULL
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- Lưu hành vi riêng để dọn sau 30 ngày mà không ảnh hưởng lịch sử giao dịch.
CREATE TABLE user_product_behavior (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(36) NOT NULL COMMENT 'Giữ nguyên khi retry để chống xử lý trùng sự kiện',
    user_id BIGINT NOT NULL COMMENT 'Lấy từ nguồn xác thực hoặc sự kiện backend đáng tin cậy',
    product_id INT NOT NULL COMMENT 'ID sản phẩm gốc, không phải biến thể',
    event_type ENUM('PRODUCT_VIEW', 'ADD_TO_CART', 'PURCHASE') NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        COMMENT 'Thời điểm hành vi, phải truyền thời điểm gốc khi xử lý sự kiện bất đồng bộ',
    PRIMARY KEY (id),
    CONSTRAINT uk_user_product_behavior_event UNIQUE (event_id),
    -- Xóa sản phẩm phải dọn hành vi liên quan mà không bị lịch sử chặn lại.
    CONSTRAINT fk_user_product_behavior_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_user_product_behavior_product (product_id),
    INDEX idx_user_product_behavior_user_created (user_id, created_at),
    -- Index hỗ trợ tra lượt xem; chống lặp đồng thời cần cơ chế nguyên tử riêng.
    INDEX idx_user_product_behavior_recent_view (user_id, product_id, event_type, created_at),
    INDEX idx_user_product_behavior_created (created_at)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Tách trạng thái chống lặp khỏi lịch sử để vẫn lưu được nhiều lượt xem hợp lệ.
CREATE TABLE user_product_view_state (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id INT NOT NULL,
    next_allowed_at TIMESTAMP(6) NOT NULL COMMENT 'Chỉ nhận lượt xem mới khi thời điểm xử lý đạt mốc này',
    PRIMARY KEY (id),
    CONSTRAINT uk_user_product_view_state_user_product UNIQUE (user_id, product_id),
    CONSTRAINT fk_user_product_view_state_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_user_product_view_state_product (product_id),
    INDEX idx_user_product_view_state_next_allowed (next_allowed_at)
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
    object_path             VARCHAR(500) NOT NULL,
    storage_bucket          VARCHAR(50) NOT NULL,
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

-- ============================================================
-- FLASH SALE
-- ============================================================

-- Lưu chương trình Flash Sale do Admin quản lý.
CREATE TABLE flash_sale_campaigns (
    -- Mã chương trình.
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- Tên chương trình.
    name VARCHAR(255) NOT NULL,

    -- Mô tả nội dung chương trình.
    description TEXT NULL,

    -- Hạn cuối Shop được đăng ký, sửa hoặc rút variant.
    registration_deadline DATETIME NOT NULL,

    -- Thời điểm bắt đầu áp dụng giá sale.
    starts_at DATETIME NOT NULL,

    -- Thời điểm kết thúc áp dụng giá sale.
    ends_at DATETIME NOT NULL,

    -- DRAFT: Shop đăng ký trước hạn; PUBLISHED: công bố cho người mua.
    -- CANCELLED: đã hủy; DONE: đã hoàn thành. Giá sale chỉ áp dụng trong giờ chạy.
    status ENUM('DRAFT', 'PUBLISHED', 'CANCELLED', 'DONE')
        NOT NULL DEFAULT 'DRAFT',

    -- ID Admin thuộc Auth service, không tạo khóa ngoại xuyên service.
    created_by BIGINT NOT NULL,

    -- Thời điểm tạo chương trình.
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Thời điểm cập nhật gần nhất.
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    -- Hạn đăng ký phải trước bắt đầu; kết thúc phải sau bắt đầu.
    CONSTRAINT chk_flash_sale_campaign_time
        CHECK (
            registration_deadline < starts_at
            AND starts_at < ends_at
        )
) ENGINE=InnoDB
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;


-- Lưu variant và điều kiện sale do Shop đăng ký.
CREATE TABLE flash_sale_items (
    -- Mã đăng ký.
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- Chương trình mà variant tham gia.
    flash_sale_campaigns_id BIGINT NOT NULL,

    -- Variant tham gia; giữ đăng ký khi variant bị xóa.
    product_variants_id INT NULL,

    -- ID chủ Shop thuộc Auth service, giữ quyền sở hữu khi variant bị xóa.
    shop_id BIGINT NOT NULL,

    -- Tên sản phẩm tại thời điểm đăng ký.
    product_name VARCHAR(255) NOT NULL,

    -- Thuộc tính tại thời điểm đăng ký, lưu JSON dạng [{"name":"...","value":"..."}].
    variant_attributes TEXT NOT NULL,

    -- Phần trăm giảm do Shop nhập; giá sale tính từ giá hiện tại của variant.
    discount_percent DECIMAL(5,2) NOT NULL,

    -- Hạn mức đăng ký, chưa phải tồn kho được giữ.
    registered_quantity INT NOT NULL,

    -- Số lượng đã đặt mua trong sale, không giảm khi hủy hoặc hoàn đơn.
    sold_quantity BIGINT NOT NULL DEFAULT 0,

    -- REGISTERED: đang đăng ký; DISABLED: Admin block. Shop xóa sẽ xóa bản ghi.
    status ENUM('REGISTERED', 'DISABLED')
        NOT NULL DEFAULT 'REGISTERED',

    -- Thời điểm tạo đăng ký.
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Thời điểm cập nhật đăng ký gần nhất.
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    -- Mỗi variant còn tồn tại chỉ có một bản ghi trong một chương trình.
    CONSTRAINT uk_flash_sale_campaign_variant
        UNIQUE (flash_sale_campaigns_id, product_variants_id),

    -- Không xóa chương trình khi còn đăng ký tham chiếu.
    CONSTRAINT fk_flash_sale_item_campaign
        FOREIGN KEY (flash_sale_campaigns_id)
        REFERENCES flash_sale_campaigns(id)
        ON DELETE RESTRICT,

    -- Xóa variant chỉ ngắt liên kết, giữ thông tin lịch sử đăng ký.
    CONSTRAINT fk_flash_sale_item_variant
        FOREIGN KEY (product_variants_id)
        REFERENCES product_variants(id)
        ON DELETE SET NULL,

    -- Phần trăm giảm phải lớn hơn 0 và nhỏ hơn 100.
    CONSTRAINT chk_flash_sale_item_discount
        CHECK (discount_percent > 0 AND discount_percent < 100),

    -- Hạn mức đăng ký phải lớn hơn 0.
    CONSTRAINT chk_flash_sale_item_quantity
        CHECK (registered_quantity > 0),

    -- Số lượng đã đặt mua không được âm.
    CONSTRAINT chk_flash_sale_item_sold_quantity
        CHECK (sold_quantity >= 0),

    -- Hỗ trợ lấy đăng ký của Shop trong từng chương trình.
    INDEX idx_flash_sale_item_shop_campaign (shop_id, flash_sale_campaigns_id),

    -- Hỗ trợ tra cứu đăng ký theo variant.
    INDEX idx_flash_sale_item_variant (product_variants_id)
) ENGINE=InnoDB
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
