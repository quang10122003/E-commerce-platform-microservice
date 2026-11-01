-- Cần MySQL 8.0.16+ để các ràng buộc CHECK được thực thi.
-- Tái sử dụng outbox_event trong order_service-db.sql; không tạo bảng nghiệp vụ thứ năm.
-- Chưa thêm index tối ưu truy vấn; MySQL vẫn tạo index phục vụ khóa và ràng buộc.
-- IF NOT EXISTS không nâng cấp cấu trúc bảng đã tồn tại.
CREATE DATABASE IF NOT EXISTS order_service_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE order_service_db;

CREATE TABLE IF NOT EXISTS checkout (
    id BIGINT NOT NULL AUTO_INCREMENT,
    -- Giữ lịch sử đơn khi tài khoản bị xóa; API tạo checkout vẫn bắt buộc đăng nhập.
    user_id BIGINT NULL,
    -- Snapshot không đổi theo hồ sơ để giữ thông tin người mua khi tài khoản bị sửa hoặc xóa.
    buyer_full_name_snapshot VARCHAR(255) NOT NULL,
    buyer_email_snapshot VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    shipping_address_snapshot VARCHAR(1000) NOT NULL,
    -- Worker cần IP khách từ request gốc để dựng URL VNPay sau khi xử lý hàng chờ.
    client_ip VARCHAR(45) NULL,
    note VARCHAR(1000) NULL,
    total_amount DECIMAL(19,0) NOT NULL,
    status ENUM('PROCESSING', 'DONE', 'CANCEL') NOT NULL DEFAULT 'PROCESSING',
    cancellation_reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_checkout_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT chk_checkout_amount CHECK (total_amount > 0),
    CONSTRAINT chk_checkout_address CHECK (CHAR_LENGTH(TRIM(shipping_address_snapshot)) > 0)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    checkout_id BIGINT NOT NULL,
    -- Giữ lịch sử đơn khi tài khoản shop ở Auth Service bị xóa.
    shop_id BIGINT NULL,
    -- Snapshot không đổi theo hồ sơ để giữ thông tin shop tại thời điểm tạo đơn.
    shop_name_snapshot VARCHAR(255) NULL,
    shop_logo_url_snapshot TEXT NULL,
    price_type ENUM('NORMAL', 'SALE') NOT NULL,
    status ENUM('PENDING_CONFIRMATION', 'PENDING_PICKUP', 'SHIPPING', 'DONE', 'CANCEL')
        NOT NULL DEFAULT 'PENDING_CONFIRMATION',
    total_amount DECIMAL(19,0) NOT NULL,
    cancellation_reason VARCHAR(500) NULL,
    cancelled_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_orders_checkout_shop_price_type UNIQUE (checkout_id, shop_id, price_type),
    CONSTRAINT fk_orders_checkout FOREIGN KEY (checkout_id)
        REFERENCES checkout (id) ON DELETE RESTRICT,
    CONSTRAINT chk_orders_amount CHECK (total_amount > 0),
    CONSTRAINT chk_orders_cancelled_at CHECK (status <> 'CANCEL' OR cancelled_at IS NOT NULL)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Không FK sang Product Service để việc xóa sản phẩm không làm mất lịch sử đơn.
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NULL,
    product_variant_id BIGINT NULL,
    product_name VARCHAR(255) NOT NULL,
    variant_attributes JSON NOT NULL,
    image_url VARCHAR(2048) NULL,
    quantity INT NOT NULL,
    original_unit_price DECIMAL(19,0) NOT NULL,
    unit_price DECIMAL(19,0) NOT NULL,
    total_amount DECIMAL(19,0) NOT NULL,
    price_type ENUM('NORMAL', 'SALE') NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_order_items_variant UNIQUE (order_id, product_variant_id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders (id) ON DELETE RESTRICT,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_attributes CHECK (JSON_TYPE(variant_attributes) = 'ARRAY'),
    CONSTRAINT chk_order_items_amount CHECK (
        original_unit_price > 0 AND unit_price > 0
        AND unit_price <= original_unit_price
        AND (price_type = 'SALE' OR unit_price = original_unit_price)
        AND total_amount = unit_price * quantity
    )
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Chỉ tạo payment cho thanh toán online; COD không có payment, UNIQUE giữ tối đa một payment mỗi checkout.
-- Loại thẻ/phương thức thực tế có thể chưa được nhà cung cấp trả về khi tạo bản ghi ban đầu.
CREATE TABLE IF NOT EXISTS payment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    checkout_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NULL,
    status ENUM('UNPAID', 'PENDING', 'DONE', 'CANCEL') NOT NULL DEFAULT 'UNPAID',
    amount DECIMAL(19,0) NULL,
    currency CHAR(3) NOT NULL DEFAULT 'VND',
    transaction_reference VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NULL,
    provider_transaction_id VARCHAR(100) CHARACTER SET ascii COLLATE ascii_bin NULL,
    bank_code VARCHAR(20) NULL,
    bank_transaction_id VARCHAR(255) NULL,
    response_code VARCHAR(2) NULL,
    transaction_status VARCHAR(2) NULL,
    payment_url TEXT NULL,
    expires_at DATETIME(6) NULL,
    paid_at DATETIME(6) NULL,
    cancelled_at DATETIME(6) NULL,
    cancellation_reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payment_checkout UNIQUE (checkout_id),
    CONSTRAINT uk_payment_reference UNIQUE (transaction_reference),
    CONSTRAINT uk_payment_provider_transaction UNIQUE (provider_transaction_id),
    CONSTRAINT fk_payment_checkout FOREIGN KEY (checkout_id)
        REFERENCES checkout (id) ON DELETE RESTRICT,
    CONSTRAINT chk_payment_currency CHECK (currency = 'VND'),
    CONSTRAINT chk_payment_amount CHECK (amount IS NULL OR amount > 0),
    CONSTRAINT chk_payment_done CHECK (
        status <> 'DONE'
        OR (amount IS NOT NULL AND paid_at IS NOT NULL)
    ),
    CONSTRAINT chk_payment_cancelled CHECK (
        status <> 'CANCEL'
        OR (cancelled_at IS NOT NULL AND paid_at IS NULL)
    ),
    CONSTRAINT chk_payment_pending CHECK (
        status <> 'PENDING'
        OR (amount IS NOT NULL AND transaction_reference IS NOT NULL AND expires_at IS NOT NULL)
    ),
    CONSTRAINT chk_payment_expiry CHECK (expires_at IS NULL OR expires_at > created_at)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Application cấp thời gian qua Clock chung; khóa hoặc cập nhật có điều kiện để tránh xử lý trùng.
-- Application kiểm tra tổng tiền liên bảng và lưu mã thao tác Product trước khi gọi ra ngoài.
-- shortcut: một payment không giữ lịch sử nhiều giao dịch độc lập; cần mở rộng khi hỗ trợ thanh toán lại hoặc hoàn tiền nhiều lần.
