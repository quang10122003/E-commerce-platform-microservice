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