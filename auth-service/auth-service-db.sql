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
	shop_name varchar(255) null,
	shop_logo_url text null,
	shop_description text null,
	shop_phone varchar(32) null,
	shop_address text null,
	is_shop_lock boolean not null default false,
	is_locked boolean not null default false,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
    UNIQUE KEY uk_outbox_event_id (event_id)
) ENGINE=InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
