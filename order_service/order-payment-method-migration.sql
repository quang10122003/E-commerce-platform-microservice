-- Chạy một lần trên order_service_db đã tồn tại; checkout-schema.sql dùng CREATE TABLE IF NOT EXISTS nên không tự thêm cột.
USE order_service_db;

ALTER TABLE orders
    ADD COLUMN payment_method ENUM('COD', 'VNPAY') NULL AFTER price_type;

-- Đơn cũ suy ra phương thức theo cùng quy tắc với saga: chỉ checkout VNPay có bản ghi payment.
UPDATE orders o
    LEFT JOIN payment p ON p.checkout_id = o.checkout_id
SET o.payment_method = IF(p.id IS NULL, 'COD', 'VNPAY')
WHERE o.payment_method IS NULL;

ALTER TABLE orders
    MODIFY COLUMN payment_method ENUM('COD', 'VNPAY') NOT NULL;
