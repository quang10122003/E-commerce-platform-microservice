-- Chạy một lần trên order_service_db đã tồn tại; checkout-schema.sql dùng CREATE TABLE IF NOT EXISTS nên không tự thêm cột.
USE order_service_db;

ALTER TABLE payment
    ADD COLUMN requested_at DATETIME(6) NULL AFTER expires_at;
