create database if not EXISTS auth_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
use auth_db;

-- bảng user
create table users(
	id bigint auto_increment primary key,
	email varchar(255) not null unique,
	password text,
	full_name varchar(255) not null,
	provider enum('LOCAL','GOOGLE','LOCAL_GOOGLE') not null default 'LOCAL',
	-- sub của Google; unique để một tài khoản Google chỉ gắn với một user.
	provider_id varchar(255) null,
	shop_name varchar(255) null,
	shop_logo_url text null,
	-- Vị trí logo trong storage để dọn file khi đổi logo, rollback hoặc xóa tài khoản.
	shop_logo_object_path varchar(500) null,
	shop_logo_storage_bucket varchar(50) null,
	avatar_url text null,
	-- Vị trí avatar trong storage (dùng chung bucket logo) để dọn file khi đổi avatar, rollback hoặc xóa tài khoản.
	avatar_object_path varchar(500) null,
	avatar_storage_bucket varchar(50) null,
	shop_description text null,
	shop_phone varchar(32) null,
	shop_address text null,
	-- Mã tỉnh theo Provinces API (provinces.open-api.vn), dùng để lọc sản phẩm theo tỉnh.
	shop_province_code int null,
	shop_created_at TIMESTAMP NULL DEFAULT NULL,
	is_shop_lock boolean not null default false,
	is_locked boolean not null default false,
	-- Xóa mềm: giữ dòng để đơn hàng/chat ở service khác vẫn tham chiếu được, thông tin cá nhân đã bị xóa.
	deleted_at TIMESTAMP NULL DEFAULT NULL,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	unique key uk_users_provider_id (provider_id)
);

-- Bảng chứa vai trò của người dùng.
create table roles(
	id bigint auto_increment primary key,
    name varchar(50)  not null unique
);
-- bảng trung gian user và role

create table user_role(
	user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
	PRIMARY KEY (user_id, role_id),
    constraint fk_user_roles_user foreign key(user_id) references users(id) on delete cascade,
    constraint fk_user_roles_role foreign key(role_id) references roles(id)  on delete cascade
);

CREATE TABLE outbox_event (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Khóa kỹ thuật, tự tăng, chỉ dùng nội bộ DB',

    event_id        CHAR(36) NOT NULL COMMENT 'UUID duy nhất của event, dùng làm key cho idempotency phía consumer',

    aggregate_type  VARCHAR(100) NOT NULL COMMENT 'Loại đối tượng nghiệp vụ phát sinh event, vd: Order, Payment',
    aggregate_id    VARCHAR(100) NOT NULL COMMENT 'ID cụ thể của đối tượng nghiệp vụ, vd: order_id = 123',

    event_type      VARCHAR(100) NOT NULL COMMENT 'Loại event, vd: OrderCreated, OrderCancelled',
    payload         JSON NOT NULL COMMENT 'Nội dung chi tiết của event, dạng JSON, sẽ được publish ra message broker',

    status          ENUM('PENDING', 'PUBLISHED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái publish: PENDING - chưa publish, PUBLISHED - đã publish thành công, FAILED - publish thất bại quá số lần retry',

    retry_count     INT NOT NULL DEFAULT 0 COMMENT 'Số lần đã thử publish event này',
    max_retry       INT NOT NULL DEFAULT 5 COMMENT 'Số lần retry tối đa trước khi đánh dấu FAILED',

    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm event được ghi vào bảng (cùng transaction với business data)',
    published_at    DATETIME NULL COMMENT 'Thời điểm publish thành công ra message broker',

    error_message   TEXT NULL COMMENT 'Thông báo lỗi lần publish gần nhất, phục vụ debug',
    CONSTRAINT uk_outbox_event_id UNIQUE (event_id),
    INDEX idx_outbox_event_status (status)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Địa chỉ giao hàng của người dùng; xóa theo tài khoản khi user bị xóa.
CREATE TABLE user_addresses (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    province   VARCHAR(100) NOT NULL,
    ward       VARCHAR(100) NOT NULL,
    detail     VARCHAR(255) NOT NULL,
    phone      VARCHAR(15)  NOT NULL,
    -- Mỗi user tối đa một địa chỉ mặc định, service khóa dòng user khi đổi để giữ ràng buộc.
    is_default BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_addresses_user (user_id)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
