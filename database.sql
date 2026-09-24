-- Tập hợp migration và dữ liệu mẫu cho auth-service và producr-service.
-- File này được gom từ các script SQL hiện có; chạy theo đúng thứ tự từ trên xuống.
-- Cần sử dụng tài khoản MySQL có quyền trên cả auth_db và product_service_db.
SET NAMES utf8mb4;

USE auth_db;

-- Nguồn: auth-service/src/main/resources/db/add_shop_fields_to_users.sql
-- Thêm thông tin shop tùy chọn cho user; user chưa có shop sẽ để NULL.
ALTER TABLE users
    ADD COLUMN shop_name VARCHAR(255) NULL DEFAULT NULL AFTER full_name,
    ADD COLUMN shop_logo_url TEXT NULL DEFAULT NULL AFTER shop_name,
    ADD COLUMN shop_description TEXT NULL DEFAULT NULL AFTER shop_logo_url,
    ADD COLUMN shop_phone VARCHAR(32) NULL DEFAULT NULL AFTER shop_description,
    ADD COLUMN shop_address TEXT NULL DEFAULT NULL AFTER shop_phone,
    ADD COLUMN shop_status VARCHAR(32) NULL DEFAULT NULL AFTER shop_address;

-- Nguồn: auth-service/src/main/resources/db/alter_shop_status_to_is_shop_lock.sql
-- Đổi trạng thái shop từ chuỗi sang cờ boolean khóa shop.
ALTER TABLE users
    CHANGE COLUMN shop_status is_shop_lock BOOLEAN NULL DEFAULT NULL AFTER shop_address;

-- User hiện tại chưa có trạng thái khóa shop nên mặc định là không khóa.
UPDATE users
SET is_shop_lock = FALSE
WHERE is_shop_lock IS NULL;

-- Bắt buộc trạng thái khóa shop luôn có giá trị true hoặc false.
ALTER TABLE users
    MODIFY COLUMN is_shop_lock BOOLEAN NOT NULL DEFAULT FALSE AFTER shop_address;

USE product_service_db;

-- Nguồn: producr-service/src/main/resources/db/add_total_sold.sql
-- Thêm tổng số lượng đã bán cho product và mặc định bằng 0.
ALTER TABLE products
    ADD COLUMN total_sold BIGINT NOT NULL DEFAULT 0 AFTER image_url;

-- Nguồn: producr-service/src/main/resources/db/outbox.sql
-- Tạo bảng outbox tổng quát cho mọi aggregate và event của product service.
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
    PRIMARY KEY (id),
    UNIQUE KEY uk_outbox_event_id (event_id),
    KEY idx_outbox_pending (status, next_attempt_at, created_at),
    KEY idx_outbox_aggregate (aggregate_type, aggregate_id, event_type)
) ENGINE = InnoDB;

-- Đưa các Product đã tồn tại vào outbox để worker đồng bộ bù sang Elasticsearch.
INSERT INTO outbox_event (
    event_id,
    aggregate_type,
    aggregate_id,
    event_type,
    payload,
    status,
    retry_count,
    max_retry,
    created_at,
    next_attempt_at
)
SELECT
    UUID(),
    'Product',
    CAST(product.id AS CHAR),
    'PRODUCT_CREATED',
    JSON_OBJECT('productId', product.id),
    'PENDING',
    0,
    5,
    COALESCE(product.created_at, NOW()),
    NOW()
FROM products product
WHERE NOT EXISTS (
    SELECT 1
    FROM outbox_event outbox
    WHERE outbox.aggregate_type = 'Product'
      AND outbox.aggregate_id = CAST(product.id AS CHAR)
      AND outbox.event_type = 'PRODUCT_CREATED'
);

-- Nguồn: producr-service/src/main/resources/db/update_product_search_demo_data.sql
-- Chuẩn hóa dữ liệu demo product để kiểm tra tìm kiếm, giá và sắp xếp theo lượt bán.
START TRANSACTION;

-- Bổ sung danh mục và thương hiệu dùng cho catalog demo.
INSERT INTO categories (id, name, image_url)
VALUES
    (1, 'Điện thoại', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80'),
    (2, 'Laptop & Máy tính', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&auto=format&fit=crop&q=80'),
    (3, 'Âm thanh', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80'),
    (4, 'Phụ kiện công nghệ', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80'),
    (5, 'Thời trang', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80'),
    (6, 'Gia dụng', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80'),
    (7, 'Máy ảnh', 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800&auto=format&fit=crop&q=80')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    image_url = VALUES(image_url);

INSERT INTO brands (id, name)
VALUES
    (1, 'Apple'),
    (2, 'Samsung'),
    (3, 'Xiaomi'),
    (4, 'Sony'),
    (5, 'Anker'),
    (6, 'Logitech'),
    (7, 'Nike'),
    (8, 'LocknLock'),
    (9, 'Canon')
ON DUPLICATE KEY UPDATE
    name = VALUES(name);

-- Cập nhật thông tin hiển thị và số lượt bán cho 30 product mẫu.
UPDATE products
SET
    categories_id = CASE id
        WHEN 16 THEN 1 WHEN 17 THEN 1 WHEN 18 THEN 1 WHEN 19 THEN 1 WHEN 20 THEN 1
        WHEN 21 THEN 2 WHEN 22 THEN 2 WHEN 23 THEN 2 WHEN 24 THEN 2
        WHEN 25 THEN 3 WHEN 26 THEN 3 WHEN 27 THEN 3 WHEN 28 THEN 3
        WHEN 29 THEN 4 WHEN 30 THEN 4 WHEN 31 THEN 4 WHEN 32 THEN 4
        WHEN 33 THEN 4 WHEN 34 THEN 4
        WHEN 35 THEN 5 WHEN 36 THEN 5
        WHEN 37 THEN 7 WHEN 38 THEN 7
        WHEN 39 THEN 6 WHEN 40 THEN 6 WHEN 41 THEN 6 WHEN 42 THEN 6 WHEN 43 THEN 6
        WHEN 44 THEN 2 WHEN 45 THEN 2
    END,
    brands_id = CASE id
        WHEN 16 THEN 1 WHEN 17 THEN 2 WHEN 18 THEN 3 WHEN 19 THEN 1 WHEN 20 THEN 2
        WHEN 21 THEN 1 WHEN 22 THEN 1 WHEN 23 THEN 2 WHEN 24 THEN 6
        WHEN 25 THEN 4 WHEN 26 THEN 1 WHEN 27 THEN 5 WHEN 28 THEN 4
        WHEN 29 THEN 5 WHEN 30 THEN 6 WHEN 31 THEN 6 WHEN 32 THEN 5
        WHEN 33 THEN 1 WHEN 34 THEN 2
        WHEN 35 THEN 7 WHEN 36 THEN 7
        WHEN 37 THEN 4 WHEN 38 THEN 9
        WHEN 39 THEN 8 WHEN 40 THEN 8 WHEN 41 THEN 3 WHEN 42 THEN 3 WHEN 43 THEN 8
        WHEN 44 THEN 1 WHEN 45 THEN 2
    END,
    name = CASE id
        WHEN 16 THEN 'iPhone 16 Pro Max 256GB'
        WHEN 17 THEN 'Samsung Galaxy S25 Ultra 256GB'
        WHEN 18 THEN 'Xiaomi 15 Ultra 512GB'
        WHEN 19 THEN 'iPhone 15 128GB'
        WHEN 20 THEN 'Samsung Galaxy A56 5G 256GB'
        WHEN 21 THEN 'MacBook Air M4 13 inch 256GB'
        WHEN 22 THEN 'MacBook Pro M4 14 inch 512GB'
        WHEN 23 THEN 'Samsung Galaxy Book5 Pro 16 inch'
        WHEN 24 THEN 'Logitech MX Keys S Wireless'
        WHEN 25 THEN 'Sony WH-1000XM6 chống ồn'
        WHEN 26 THEN 'AirPods Pro 2 USB-C'
        WHEN 27 THEN 'Anker Soundcore Liberty 4 NC'
        WHEN 28 THEN 'Sony SRS-XB100 Bluetooth Speaker'
        WHEN 29 THEN 'Anker 737 Power Bank 24000mAh'
        WHEN 30 THEN 'Logitech G Pro X Superlight 2'
        WHEN 31 THEN 'Logitech G915 TKL Wireless'
        WHEN 32 THEN 'Anker Nano 65W GaN Charger'
        WHEN 33 THEN 'Apple Watch Series 10 46mm'
        WHEN 34 THEN 'Samsung Galaxy Watch7 44mm'
        WHEN 35 THEN 'Nike Air Max Dn chính hãng'
        WHEN 36 THEN 'Nike Court Vision Low'
        WHEN 37 THEN 'Sony Alpha ZV-E10 II Kit'
        WHEN 38 THEN 'Canon EOS R50 Kit 18-45mm'
        WHEN 39 THEN 'LocknLock Metro Mug 475ml'
        WHEN 40 THEN 'LocknLock hộp bảo quản thực phẩm 5 món'
        WHEN 41 THEN 'Xiaomi Robot Vacuum S20'
        WHEN 42 THEN 'Xiaomi Smart Air Purifier 4'
        WHEN 43 THEN 'LocknLock bình giữ nhiệt 800ml'
        WHEN 44 THEN 'iPad Air M3 11 inch 128GB'
        WHEN 45 THEN 'Samsung Galaxy Tab S10+ 256GB'
    END,
    description = CASE id
        WHEN 16 THEN 'Chip A18 Pro, màn hình ProMotion 6.9 inch, camera chuyên nghiệp và pin dùng cả ngày.'
        WHEN 17 THEN 'Màn hình Dynamic AMOLED 2X, camera 200MP, S Pen và hiệu năng Snapdragon cao cấp.'
        WHEN 18 THEN 'Camera Leica, cảm biến lớn, màn hình AMOLED 120Hz và sạc nhanh 90W.'
        WHEN 19 THEN 'Màn hình Super Retina XDR 6.1 inch, camera kép 48MP và chip A16 Bionic.'
        WHEN 20 THEN 'Màn hình Super AMOLED 120Hz, camera chống rung quang học và pin 5000mAh.'
        WHEN 21 THEN 'MacBook mỏng nhẹ dùng chip Apple M4, RAM 16GB và thời lượng pin lên đến 18 giờ.'
        WHEN 22 THEN 'MacBook Pro chip M4 Pro, màn hình Liquid Retina XDR và hiệu năng chuyên nghiệp.'
        WHEN 23 THEN 'Laptop màn hình AMOLED 3K 120Hz, thiết kế mỏng nhẹ và pin dung lượng cao.'
        WHEN 24 THEN 'Bàn phím cơ thấp không dây, kết nối đa thiết bị và pin dùng nhiều tuần.'
        WHEN 25 THEN 'Tai nghe over-ear chống ồn chủ động, âm thanh Hi-Res và pin lên đến 30 giờ.'
        WHEN 26 THEN 'Tai nghe chống ồn chủ động, hộp sạc USB-C và âm thanh không gian cá nhân hóa.'
        WHEN 27 THEN 'Tai nghe true wireless chống ồn thích ứng, pin 50 giờ và kết nối đa thiết bị.'
        WHEN 28 THEN 'Loa Bluetooth nhỏ gọn, chống nước IP67 và thời lượng phát nhạc lên đến 16 giờ.'
        WHEN 29 THEN 'Pin dự phòng 24000mAh, công suất 140W và màn hình theo dõi dung lượng.'
        WHEN 30 THEN 'Chuột gaming không dây siêu nhẹ, cảm biến HERO 2 và độ trễ cực thấp.'
        WHEN 31 THEN 'Bàn phím gaming không dây cơ học, switch tactile và đèn RGB Lightsync.'
        WHEN 32 THEN 'Củ sạc GaN công suất 65W, hai cổng USB-C và hỗ trợ sạc nhanh PD.'
        WHEN 33 THEN 'Apple Watch màn hình lớn, theo dõi sức khỏe toàn diện và chống nước 50 mét.'
        WHEN 34 THEN 'Đồng hồ thông minh AMOLED, GPS kép, theo dõi giấc ngủ và sức khỏe.'
        WHEN 35 THEN 'Giày thể thao đệm êm, thiết kế hiện đại phù hợp chạy bộ và sử dụng hằng ngày.'
        WHEN 36 THEN 'Sneaker phong cách cổ điển, đế cao su bền và phối đồ linh hoạt.'
        WHEN 37 THEN 'Máy ảnh vlog APS-C, lấy nét chủ thể nhanh và quay video 4K chất lượng cao.'
        WHEN 38 THEN 'Máy ảnh mirrorless nhỏ gọn, lấy nét Dual Pixel và bộ kit ống kính đa dụng.'
        WHEN 39 THEN 'Ly giữ nhiệt inox 304 dung tích 475ml, nắp kín và dễ vệ sinh.'
        WHEN 40 THEN 'Bộ hộp bảo quản thực phẩm nhiều kích thước, nhựa an toàn và nắp kín khí.'
        WHEN 41 THEN 'Robot hút bụi lau nhà, điều hướng laser và lực hút mạnh cho căn hộ hiện đại.'
        WHEN 42 THEN 'Máy lọc không khí cho phòng lớn, lọc bụi mịn PM2.5 và điều khiển qua ứng dụng.'
        WHEN 43 THEN 'Bình giữ nhiệt inox 316 dung tích 800ml, giữ nóng lạnh nhiều giờ.'
        WHEN 44 THEN 'iPad chip M3, màn hình Liquid Retina 11 inch và hỗ trợ Apple Pencil Pro.'
        WHEN 45 THEN 'Tablet AMOLED 12.4 inch, S Pen đi kèm và hiệu năng mạnh cho học tập, giải trí.'
    END,
    image_url = CASE id
        WHEN 16 THEN 'https://images.unsplash.com/photo-1592286927505-2fd0c8f3d9f6?w=800&auto=format&fit=crop&q=80'
        WHEN 17 THEN 'https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=800&auto=format&fit=crop&q=80'
        WHEN 18 THEN 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80'
        WHEN 19 THEN 'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&auto=format&fit=crop&q=80'
        WHEN 20 THEN 'https://images.unsplash.com/photo-1567581935884-3349723552ca?w=800&auto=format&fit=crop&q=80'
        WHEN 21 THEN 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&auto=format&fit=crop&q=80'
        WHEN 22 THEN 'https://images.unsplash.com/photo-1517336714739-489689fd1ca8?w=800&auto=format&fit=crop&q=80'
        WHEN 23 THEN 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&auto=format&fit=crop&q=80'
        WHEN 24 THEN 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80'
        WHEN 25 THEN 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80'
        WHEN 26 THEN 'https://images.unsplash.com/photo-1606220945770-b5b6c2c55bf1?w=800&auto=format&fit=crop&q=80'
        WHEN 27 THEN 'https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&auto=format&fit=crop&q=80'
        WHEN 28 THEN 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800&auto=format&fit=crop&q=80'
        WHEN 29 THEN 'https://images.unsplash.com/photo-1609592424851-9f2f2f5e9b7e?w=800&auto=format&fit=crop&q=80'
        WHEN 30 THEN 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?w=800&auto=format&fit=crop&q=80'
        WHEN 31 THEN 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80'
        WHEN 32 THEN 'https://images.unsplash.com/photo-1622445262464-84b1456045b6?w=800&auto=format&fit=crop&q=80'
        WHEN 33 THEN 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop&q=80'
        WHEN 34 THEN 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop&q=80'
        WHEN 35 THEN 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80'
        WHEN 36 THEN 'https://images.unsplash.com/photo-1495555961986-6d4c1ecb7be3?w=800&auto=format&fit=crop&q=80'
        WHEN 37 THEN 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&auto=format&fit=crop&q=80'
        WHEN 38 THEN 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&auto=format&fit=crop&q=80'
        WHEN 39 THEN 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80'
        WHEN 40 THEN 'https://images.unsplash.com/photo-1583947215259-38e31be8751f?w=800&auto=format&fit=crop&q=80'
        WHEN 41 THEN 'https://images.unsplash.com/photo-1558317374-067fb5f30001?w=800&auto=format&fit=crop&q=80'
        WHEN 42 THEN 'https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800&auto=format&fit=crop&q=80'
        WHEN 43 THEN 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80'
        WHEN 44 THEN 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=800&auto=format&fit=crop&q=80'
        WHEN 45 THEN 'https://images.unsplash.com/photo-1561154464-82e9adf32764?w=800&auto=format&fit=crop&q=80'
    END,
    total_sold = CASE id
        WHEN 16 THEN 1850 WHEN 17 THEN 2200 WHEN 18 THEN 1650 WHEN 19 THEN 4200 WHEN 20 THEN 3850
        WHEN 21 THEN 980 WHEN 22 THEN 630 WHEN 23 THEN 420 WHEN 24 THEN 2100
        WHEN 25 THEN 1750 WHEN 26 THEN 3500 WHEN 27 THEN 2800 WHEN 28 THEN 1600
        WHEN 29 THEN 3200 WHEN 30 THEN 1450 WHEN 31 THEN 760 WHEN 32 THEN 5100
        WHEN 33 THEN 1220 WHEN 34 THEN 980
        WHEN 35 THEN 1850 WHEN 36 THEN 2250
        WHEN 37 THEN 540 WHEN 38 THEN 680
        WHEN 39 THEN 4200 WHEN 40 THEN 3800 WHEN 41 THEN 730 WHEN 42 THEN 1150 WHEN 43 THEN 2900
        WHEN 44 THEN 870 WHEN 45 THEN 640
    END,
    is_active = 'ACTIVE'
WHERE id BETWEEN 16 AND 45;

-- Đồng bộ giá bán và tồn kho của variant tương ứng với từng product.
UPDATE product_variants
SET
    price = CASE products_id
        WHEN 16 THEN 29990000 WHEN 17 THEN 27990000 WHEN 18 THEN 22990000 WHEN 19 THEN 18490000 WHEN 20 THEN 9990000
        WHEN 21 THEN 27990000 WHEN 22 THEN 45990000 WHEN 23 THEN 32990000 WHEN 24 THEN 2290000
        WHEN 25 THEN 8990000 WHEN 26 THEN 5990000 WHEN 27 THEN 1990000 WHEN 28 THEN 1190000
        WHEN 29 THEN 2490000 WHEN 30 THEN 3290000 WHEN 31 THEN 4490000 WHEN 32 THEN 890000
        WHEN 33 THEN 11990000 WHEN 34 THEN 6490000
        WHEN 35 THEN 4290000 WHEN 36 THEN 2190000
        WHEN 37 THEN 24990000 WHEN 38 THEN 18990000
        WHEN 39 THEN 329000 WHEN 40 THEN 499000 WHEN 41 THEN 5990000 WHEN 42 THEN 3490000 WHEN 43 THEN 389000
        WHEN 44 THEN 16990000 WHEN 45 THEN 23990000
    END,
    stock_quantity = CASE products_id
        WHEN 16 THEN 42 WHEN 17 THEN 55 WHEN 18 THEN 38 WHEN 19 THEN 76 WHEN 20 THEN 120
        WHEN 21 THEN 24 WHEN 22 THEN 18 WHEN 23 THEN 31 WHEN 24 THEN 86
        WHEN 25 THEN 44 WHEN 26 THEN 95 WHEN 27 THEN 138 WHEN 28 THEN 72
        WHEN 29 THEN 64 WHEN 30 THEN 48 WHEN 31 THEN 35 WHEN 32 THEN 210
        WHEN 33 THEN 27 WHEN 34 THEN 41
        WHEN 35 THEN 63 WHEN 36 THEN 88
        WHEN 37 THEN 16 WHEN 38 THEN 22
        WHEN 39 THEN 180 WHEN 40 THEN 145 WHEN 41 THEN 29 WHEN 42 THEN 37 WHEN 43 THEN 160
        WHEN 44 THEN 34 WHEN 45 THEN 28
    END
WHERE products_id BETWEEN 16 AND 45;

COMMIT;

-- Nguồn: producr-service/src/main/resources/db/seed_scroll_test_products_ha_noi.sql
-- Seed 100 sản phẩm để kiểm tra scroll và bộ lọc tỉnh/thành.
-- Mỗi sản phẩm có một variant và một outbox event để scheduler đồng bộ sang Elasticsearch.
START TRANSACTION;

-- Dùng chuỗi UTF-8 dạng hex để script không phụ thuộc encoding của mysql client.
SET @location_ha_noi = CONVERT(0x48C3A0204EE1BB9969 USING utf8mb4) COLLATE utf8mb4_unicode_ci;

-- Bảo đảm các khóa tham chiếu tối thiểu đã tồn tại trong môi trường demo.
INSERT IGNORE INTO categories (id, name, image_url)
VALUES
    (1, 'Điện thoại', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80'),
    (2, 'Laptop & Máy tính', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800&auto=format&fit=crop&q=80'),
    (3, 'Âm thanh', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80'),
    (4, 'Phụ kiện công nghệ', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80'),
    (5, 'Thời trang', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&auto=format&fit=crop&q=80'),
    (6, 'Gia dụng', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80'),
    (7, 'Máy ảnh', 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99e?w=800&auto=format&fit=crop&q=80');

INSERT IGNORE INTO brands (id, name)
VALUES
    (1, 'Apple'),
    (2, 'Samsung'),
    (3, 'Xiaomi'),
    (4, 'Sony'),
    (5, 'Anker'),
    (6, 'Logitech'),
    (7, 'Nike'),
    (8, 'LocknLock'),
    (9, 'Canon');

-- Sinh dãy 1..100 mà không cần hard-code 100 dòng sản phẩm.
CREATE TEMPORARY TABLE tmp_scroll_product_numbers (
    number INT NOT NULL PRIMARY KEY
);

INSERT INTO tmp_scroll_product_numbers (number)
WITH RECURSIVE numbers AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM numbers
    WHERE number < 100
)
SELECT number
FROM numbers;

-- Chuẩn hóa các bản ghi đã seed trước đó nếu client cũ làm hỏng ký tự tiếng Việt.
UPDATE products product
JOIN product_variants variant ON variant.products_id = product.id
SET
    product.name = CONCAT('Scroll Test ', @location_ha_noi, ' ', LPAD(SUBSTRING(variant.sku, 11), 3, '0')),
    product.description = CONCAT('Demo product for infinite scroll in ', @location_ha_noi, ', item ', SUBSTRING(variant.sku, 11), '.'),
    product.image_url = 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80',
    product.is_active = 'ACTIVE'
WHERE variant.sku LIKE 'HN-SCROLL-%';

-- Tạo sản phẩm demo, có thể chạy lại mà không tạo bản ghi trùng tên.
INSERT INTO products (
    user_id,
    categories_id,
    brands_id,
    name,
    description,
    image_url,
    total_sold,
    is_active,
    created_at,
    updated_at
)
SELECT
    1,
    1 + MOD(numbers.number - 1, 7),
    1 + MOD(numbers.number - 1, 9),
    CONCAT('Scroll Test ', @location_ha_noi, ' ', LPAD(numbers.number, 3, '0')),
    CONCAT('Demo product for infinite scroll in ', @location_ha_noi, ', item ', LPAD(numbers.number, 3, '0'), '.'),
    'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80',
    50 + numbers.number * 37,
    'ACTIVE',
    NOW(),
    NOW()
FROM tmp_scroll_product_numbers numbers
WHERE NOT EXISTS (
    SELECT 1
    FROM products product
    WHERE product.name = CONCAT('Scroll Test ', @location_ha_noi, ' ', LPAD(numbers.number, 3, '0'))
);

-- Tạo một variant cho từng sản phẩm demo để sản phẩm có thể hiển thị giá và tồn kho.
INSERT INTO product_variants (
    products_id,
    sku,
    price,
    stock_quantity,
    created_at
)
SELECT
    product.id,
    CONCAT('HN-SCROLL-', LPAD(numbers.number, 3, '0')),
    250000 + numbers.number * 125000,
    20 + MOD(numbers.number, 181),
    NOW()
FROM tmp_scroll_product_numbers numbers
JOIN products product
    ON product.name = CONCAT('Scroll Test ', @location_ha_noi, ' ', LPAD(numbers.number, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1
    FROM product_variants variant
    WHERE variant.sku = CONCAT('HN-SCROLL-', LPAD(numbers.number, 3, '0'))
);

-- Đưa sản phẩm mới vào outbox để ProductOutboxRetryScheduler đồng bộ cả product và location sang ES.
INSERT INTO outbox_event (
    event_id,
    aggregate_type,
    aggregate_id,
    event_type,
    payload,
    status,
    retry_count,
    max_retry,
    created_at,
    next_attempt_at
)
SELECT
    UUID(),
    'Product',
    CAST(product.id AS CHAR) COLLATE utf8mb4_unicode_ci,
    'PRODUCT_CREATED',
    JSON_OBJECT(
        'productId', product.id,
        'location', @location_ha_noi
    ),
    'PENDING',
    0,
    5,
    COALESCE(product.created_at, NOW()),
    NOW()
FROM tmp_scroll_product_numbers numbers
JOIN products product
    ON product.name = CONCAT('Scroll Test ', @location_ha_noi, ' ', LPAD(numbers.number, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1
    FROM outbox_event outbox
    WHERE outbox.aggregate_type = 'Product'
      AND outbox.aggregate_id = CAST(product.id AS CHAR) COLLATE utf8mb4_unicode_ci
      AND outbox.event_type = 'PRODUCT_CREATED'
);

-- Gửi lại event của 100 product để ES nhận location đã chuẩn hóa.
UPDATE outbox_event outbox
JOIN products product ON outbox.aggregate_id = CAST(product.id AS CHAR) COLLATE utf8mb4_unicode_ci
JOIN product_variants variant ON variant.products_id = product.id
SET
    outbox.payload = JSON_OBJECT('productId', product.id, 'location', @location_ha_noi),
    outbox.status = 'PENDING',
    outbox.retry_count = 0,
    outbox.next_attempt_at = NOW(),
    outbox.published_at = NULL,
    outbox.error_message = NULL
WHERE variant.sku LIKE 'HN-SCROLL-%'
  AND outbox.aggregate_type = 'Product'
  AND outbox.event_type = 'PRODUCT_CREATED';

COMMIT;

DROP TEMPORARY TABLE tmp_scroll_product_numbers;

-- Nguồn: producr-service/src/main/resources/db/seed_scroll_test_products_a.sql
-- Seed 100 sản phẩm có tên bắt đầu bằng chữ a để kiểm tra keyword và scroll.
-- Sản phẩm được đưa qua outbox để đồng bộ sang Elasticsearch theo flow hiện tại.
START TRANSACTION;

-- Dùng chuỗi UTF-8 dạng hex để không phụ thuộc encoding của mysql client.
SET @location_ha_noi = CONVERT(0x48C3A0204EE1BB9969 USING utf8mb4) COLLATE utf8mb4_unicode_ci;

-- Sinh dãy 1..100 mà không cần hard-code từng sản phẩm.
CREATE TEMPORARY TABLE tmp_a_scroll_product_numbers (
    number INT NOT NULL PRIMARY KEY
);

INSERT INTO tmp_a_scroll_product_numbers (number)
WITH RECURSIVE numbers AS (
    SELECT 1 AS number
    UNION ALL
    SELECT number + 1
    FROM numbers
    WHERE number < 100
)
SELECT number
FROM numbers;

-- Tạo product có tên bắt đầu bằng chữ a và tránh tạo trùng khi chạy lại script.
INSERT INTO products (
    user_id,
    categories_id,
    brands_id,
    name,
    description,
    image_url,
    total_sold,
    is_active,
    created_at,
    updated_at
)
SELECT
    1,
    1 + MOD(numbers.number - 1, 7),
    1 + MOD(numbers.number - 1, 9),
    CONCAT('a Scroll Test ', @location_ha_noi, ' ', LPAD(numbers.number, 3, '0')),
    CONCAT('Demo product bắt đầu bằng a để kiểm tra tìm kiếm và scroll, item ', LPAD(numbers.number, 3, '0'), '.'),
    'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80',
    100 + numbers.number * 29,
    'ACTIVE',
    NOW(),
    NOW()
FROM tmp_a_scroll_product_numbers numbers
WHERE NOT EXISTS (
    SELECT 1
    FROM products product
    WHERE product.name = CONCAT('a Scroll Test ', @location_ha_noi, ' ', LPAD(numbers.number, 3, '0'))
);

-- Tạo variant riêng để product có giá, tồn kho và SKU phục vụ hiển thị.
INSERT INTO product_variants (
    products_id,
    sku,
    price,
    stock_quantity,
    created_at
)
SELECT
    product.id,
    CONCAT('A-SCROLL-', LPAD(numbers.number, 3, '0')),
    150000 + numbers.number * 95000,
    30 + MOD(numbers.number, 171),
    NOW()
FROM tmp_a_scroll_product_numbers numbers
JOIN products product
    ON product.name = CONCAT('a Scroll Test ', @location_ha_noi, ' ', LPAD(numbers.number, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1
    FROM product_variants variant
    WHERE variant.sku = CONCAT('A-SCROLL-', LPAD(numbers.number, 3, '0'))
);

-- Đưa product vào outbox để scheduler index product cùng location vào Elasticsearch.
INSERT INTO outbox_event (
    event_id,
    aggregate_type,
    aggregate_id,
    event_type,
    payload,
    status,
    retry_count,
    max_retry,
    created_at,
    next_attempt_at
)
SELECT
    UUID(),
    'Product',
    CAST(product.id AS CHAR) COLLATE utf8mb4_unicode_ci,
    'PRODUCT_CREATED',
    JSON_OBJECT(
        'productId', product.id,
        'location', @location_ha_noi
    ),
    'PENDING',
    0,
    5,
    COALESCE(product.created_at, NOW()),
    NOW()
FROM tmp_a_scroll_product_numbers numbers
JOIN products product
    ON product.name = CONCAT('a Scroll Test ', @location_ha_noi, ' ', LPAD(numbers.number, 3, '0'))
WHERE NOT EXISTS (
    SELECT 1
    FROM outbox_event outbox
    WHERE outbox.aggregate_type = 'Product'
      AND outbox.aggregate_id = CAST(product.id AS CHAR) COLLATE utf8mb4_unicode_ci
      AND outbox.event_type = 'PRODUCT_CREATED'
);

COMMIT;

DROP TEMPORARY TABLE tmp_a_scroll_product_numbers;


