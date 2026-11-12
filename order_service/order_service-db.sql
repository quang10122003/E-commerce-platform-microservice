CREATE DATABASE IF NOT EXISTS order_service_db
    CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;

USE order_service_db;

CREATE TABLE IF NOT EXISTS carts (
	id         BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id    BIGINT NOT NULL UNIQUE,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
    CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS cart_items (
	id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id            BIGINT NOT NULL,
	product_variant_id BIGINT NOT NULL,
    quantity           INT NOT NULL,
	created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

	CONSTRAINT uk_cart_items_cart_variant UNIQUE (cart_id, product_variant_id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,

    CONSTRAINT chk_cart_items_quantity CHECK (quantity > 0)
    ) ENGINE = InnoDB
    CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;
-- Giữ sự kiện độc lập với giỏ để việc xóa giỏ không làm mất event chưa gửi.
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
    created_at DATETIME(6) NOT NULL,
    next_attempt_at DATETIME(6) NOT NULL,
    published_at DATETIME(6) NULL,
    error_message TEXT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_outbox_event_event_id UNIQUE (event_id),
    INDEX idx_outbox_event_pending (status, next_attempt_at, id)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Cần MySQL 8.0.16+ để các ràng buộc CHECK được thực thi.
-- Chưa thêm index tối ưu cho checkout; MySQL vẫn tạo index phục vụ khóa và ràng buộc.
-- IF NOT EXISTS không nâng cấp cấu trúc bảng đã tồn tại.
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
    INDEX idx_checkout_user (user_id),
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
    payment_method ENUM('COD', 'VNPAY') NOT NULL,
    status ENUM('PENDING_CONFIRMATION', 'PENDING_PICKUP', 'SHIPPING', 'DONE', 'CANCEL')
        NOT NULL DEFAULT 'PENDING_CONFIRMATION',
    total_amount DECIMAL(19,0) NOT NULL,
    -- Tác nhân hủy đơn; lý do lỗi thanh toán/checkout xem ở bảng payment/checkout.
    cancelled_by ENUM('USER', 'SHOP', 'SYSTEM') NULL,
    -- Lý do do user/shop chọn khi hủy qua API; NULL khi hệ thống tự hủy.
    cancel_reason ENUM('CHANGE_MIND', 'WRONG_ADDRESS', 'FOUND_CHEAPER', 'DUPLICATE_ORDER',
        'OUT_OF_STOCK', 'CANNOT_DELIVER', 'BUYER_UNREACHABLE', 'PRODUCT_INFO_ERROR', 'OTHER') NULL,
    cancelled_at DATETIME(6) NULL,
    -- Mốc shop (hoặc hệ thống với VNPay) xác nhận và mốc giao cho đơn vị vận chuyển, dùng cho dòng thời gian đơn.
    confirmed_at DATETIME(6) NULL,
    shipped_at DATETIME(6) NULL,
    -- Mốc đơn tự chuyển sang hoàn thành bởi job sau khi đang giao đủ thời gian.
    delivered_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_orders_checkout_shop_price_type UNIQUE (checkout_id, shop_id, price_type),
    -- Job tự hoàn thành đơn quét theo trạng thái và mốc giao cho đơn vị vận chuyển.
    INDEX idx_orders_status_shipped_at (status, shipped_at),
    INDEX idx_orders_shop_created_at (shop_id, created_at),
    CONSTRAINT fk_orders_checkout FOREIGN KEY (checkout_id)
        REFERENCES checkout (id) ON DELETE RESTRICT,
    CONSTRAINT chk_orders_amount CHECK (total_amount > 0),
    CONSTRAINT chk_orders_cancelled CHECK (status <> 'CANCEL' OR (cancelled_at IS NOT NULL AND cancelled_by IS NOT NULL))
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
    requested_at DATETIME(6) NULL,
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

-- Cần MySQL 8.0.16+ để các ràng buộc CHECK được thực thi.
-- Inbox được commit cùng nghiệp vụ, chống xử lý lại bằng event_id.
-- UNIQUE event_id chặn hai consumer xử lý đồng thời cùng event; lỗi nghiệp vụ phải rollback cả Inbox.
CREATE TABLE IF NOT EXISTS inbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_id VARCHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON NOT NULL,
    processed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_inbox_event_id UNIQUE (event_id)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
