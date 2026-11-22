# E-commerce Platform Microservice

Backend sàn thương mại điện tử theo kiến trúc microservice (Spring Boot 4, Java 21, Spring Cloud), gồm 7 service giao tiếp qua Eureka, API Gateway và Kafka.

## Thành phần

| Service | Thư mục | Port | Vai trò |
|---|---|---|---|
| discovery-server | `discovery-server/` | 8761 | Eureka registry |
| gateway | `getway/` | 8080 | API Gateway, cửa vào duy nhất cho FE |
| auth-service | `auth-service/` | 8081 | Tài khoản, JWT, OAuth2 Google, shop |
| notification-service | `notification-service/` | 8082 | Email, chat, banner, SSE/WebSocket |
| product-service | `producr-service/` | 8083 | Sản phẩm, danh mục, flash sale, tìm kiếm (Elasticsearch) |
| order-service | `order_service/` | 8084 | Giỏ hàng, checkout, đơn hàng, thanh toán VNPay, đánh giá |
| monitoring-service | `monitoring-service/` | 8085 | Trạng thái hệ thống cho trang admin |

Hạ tầng (khai báo trong `infra/`, mỗi thành phần một file `<tên>.yml`):

| Thành phần | Port | Ghi chú |
|---|---|---|
| MySQL 8.4 | 3306 | 4 database: `auth_db`, `product_service_db`, `order_service_db`, `notifiservice_db` |
| Redis 7 | 6379 | |
| Kafka 7.6 (KRaft) | 9092 | Trong mạng Docker dùng `kafka:29092` |
| Kafka UI | 9000 | http://localhost:9000 |
| Elasticsearch 9.4 | 9200 | |
| Elasticvue | 8090 | http://localhost:8090 |
| ngrok | 4040 | Public gateway cho IPN VNPay |
| Prometheus | 9090 | http://localhost:9090 |
| Grafana | 3001 | http://localhost:3001 |

Module `common/` là thư viện dùng chung, mọi service phụ thuộc vào nó.

## Yêu cầu

- Docker Desktop (Docker Compose v2.20 trở lên để dùng `include`)
- JDK 21 và Maven 3.9 (chỉ cần khi chạy service local)

## Cấu hình biến môi trường

Mỗi nơi có file `.env` riêng, tạo từ `.env.example` cùng thư mục rồi điền giá trị:

| File | Dùng cho |
|---|---|
| `infra/.env` | MySQL root password, ngrok token, tài khoản Grafana, Telegram |
| `<service>/.env` | Bí mật của từng service: tài khoản DB, khóa JWT, Supabase, VNPay, Gemini, Resend… |

```powershell
Copy-Item infra/.env.example infra/.env
```

Làm tương tự cho từng thư mục service. `PASSWORD_DATABASE` của các service phải trùng `MYSQL_ROOT_PASSWORD` trong `infra/.env` (các service đăng nhập MySQL bằng `root`).

## Profile cấu hình

Mỗi service có 3 file trong `src/main/resources`:

- `application.yaml`: cấu hình chung, profile mặc định là `local`
- `application-local.yaml`: địa chỉ hạ tầng khi chạy trên máy (`localhost`)
- `application-docker.yaml`: địa chỉ hạ tầng khi chạy trong Docker (`mysql`, `kafka:29092`, `redis`, `elasticsearch`, `discovery-server`)

Profile chọn qua biến `SPRING_PROFILES_ACTIVE`. Muốn trỏ tới hạ tầng khác (server, cloud) không cần sửa code, chỉ ghi đè bằng biến môi trường như `SPRING_DATASOURCE_URL`, `SPRING_KAFKA_BOOTSTRAPSERVERS`, `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`.

## Dữ liệu MySQL

Container MySQL chạy các file trong `infra/mysql/init/` **chỉ khi volume dữ liệu còn trống** (lần khởi tạo đầu tiên).

- `infra/mysql/init/init.sql` (tạo database, bảng và dữ liệu mẫu) không được commit vì chứa dữ liệu thật; xin file này từ người quản lý dự án.
- Nếu không có, chạy lần lượt file schema ở gốc từng service để tạo database và bảng trống: `auth-service/auth-service-db.sql`, `producr-service/product-service-db.sql`, `order_service/order_service-db.sql`, `notification-service/notificartion_db.sql`.

Sau khi nạp lại dữ liệu sản phẩm cần đồng bộ lại index Elasticsearch (`product_search`, `product_embedding`), xem mục [Đồng bộ dữ liệu tìm kiếm](#đồng-bộ-dữ-liệu-tìm-kiếm-elasticsearch).

---

## 1. Chạy toàn bộ bằng Docker

Bật hạ tầng và cả 7 service bằng file `docker-compose.yml` ở gốc repo:

```powershell
docker compose up -d --build
```

Xem log, dừng hệ thống:

```powershell
docker compose logs -f gateway
```

```powershell
docker compose down
```

Mọi container nằm chung mạng `ecommerce-net`, service tự dùng profile `docker`. Lần đầu build mất vài phút vì tải image Maven/JRE và dependency; các lần sau dùng cache.

### Đồng bộ dữ liệu tìm kiếm (Elasticsearch)

Elasticsearch chỉ tự cập nhật khi sản phẩm được tạo hoặc sửa qua API. Sau khi build và chạy lần đầu, hoặc sau khi nạp lại dữ liệu MySQL hay xóa volume Elasticsearch, chỉ mục tìm kiếm còn trống hoặc thiếu sản phẩm. Khi đó trang tìm kiếm không ra kết quả, và cần gọi API đồng bộ lại từ MySQL.

1. Chờ các service lên đủ: mở `http://localhost:8761`, Eureka phải hiện đủ 7 service `UP`.
2. Gọi API đồng bộ qua Gateway:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/products/search-index/sync -TimeoutSec 900
```

Kết quả trả về số sản phẩm đã đồng bộ và danh sách sản phẩm lỗi:

```json
{ "totalProducts": 500, "syncedProducts": 500, "failedProductIds": [] }
```

- API tạo lại hai index `product_search` và `product_embedding` theo đúng mapping nếu chưa có, rồi đồng bộ toàn bộ sản phẩm từ MySQL.
- Embedding được tạo bằng Gemini nên lần đầu có thể mất vài phút. Gemini giới hạn số lần gọi (lỗi 429) nên có thể có sản phẩm nằm trong `failedProductIds`; gọi lại API thì chỉ những sản phẩm này được tạo embedding lại, sản phẩm không đổi nội dung sẽ được bỏ qua.
- Nếu nhận lỗi 503 hoặc 500 ngay sau khi khởi động, Gateway có thể chưa nhận ra product-service; chờ thêm khoảng 30 giây rồi gọi lại.
- Kiểm tra số tài liệu trong index: `Invoke-RestMethod http://localhost:9200/product_search/_count`.

## 2. Chạy local (service trên máy, hạ tầng bằng Docker)

**Bước 1: bật hạ tầng**

```powershell
cd infra; docker compose up -d
```

**Bước 2: cài module `common` vào kho Maven local** (làm lại mỗi khi `common` thay đổi)

```powershell
mvn -f common/pom.xml install -DskipTests
```

**Bước 3: chạy các service** theo thứ tự `discovery-server` → các service còn lại → `gateway`.

- **IntelliJ:** chạy class `*Application` của từng service, đặt *Working directory* là thư mục service để Spring đọc được file `.env` cùng thư mục.
- **Dòng lệnh:** đứng trong thư mục service rồi chạy, ví dụ:

```powershell
cd auth-service; ./mvnw spring-boot:run
```

Service tự dùng profile `local`, kết nối hạ tầng qua `localhost`. `common/` và `monitoring-service/` không có Maven wrapper, dùng `mvn` thay cho `./mvnw`.

## 3. Chạy từng service

### Bằng Docker

Mỗi service có `Dockerfile` và file compose `<tên-service>.yml` trong thư mục của nó. Hạ tầng (`infra`) nên được bật trước.

Qua file compose gốc (build lại và khởi động lại riêng một service):

```powershell
docker compose up -d --build auth-service
```

Hoặc chỉ dùng file của service (chạy được cả khi tách thành repo riêng):

```powershell
docker compose -f auth-service/auth-service.yml up -d --build
```

Build image thủ công (context là gốc repo vì cần module `common`):

```powershell
docker build -f auth-service/Dockerfile -t ecommerce/auth-service .
```

```powershell
docker run -d --name ecom-auth-service --network ecommerce-net --env-file auth-service/.env -e SPRING_PROFILES_ACTIVE=docker -p 8081:8081 ecommerce/auth-service
```

Tên service trong compose: `discovery-server`, `gateway`, `auth-service`, `product-service`, `order-service`, `notification-service`, `monitoring-service`. Container mang tiền tố `ecom-` (ví dụ `ecom-auth-service`); trong mạng `ecommerce-net` các container gọi nhau bằng tên service (`mysql`, `kafka`, `discovery-server`…), không phụ thuộc tên container. Khi chạy bằng `docker run` mà các service khác cần gọi tới (như discovery-server), thêm `--network-alias <tên-service>`.

### Local

Bật hạ tầng và `discovery-server` trước, sau đó chạy riêng service cần làm việc như ở mục 2, bước 3. Có thể kết hợp: phần lớn service chạy bằng Docker, riêng service đang sửa chạy trong IntelliJ, miễn là không trùng port (dừng container của service đó trước).

---

## Lưu ý

- **Trùng port:** không chạy cùng lúc một service vừa trong IntelliJ vừa trong Docker. Container hạ tầng tạo bằng `docker run` trước đây (`mysql`, `redis`, `kafka`…) đang giữ các port 3306, 6379, 9092… nên cần `docker stop` trước khi dùng compose.
- **ngrok** cần `NGROK_AUTHTOKEN` trong `infra/.env`; thiếu biến này compose sẽ báo lỗi và không chạy.
- **Frontend** nằm ở repo riêng: trang người dùng chạy tại http://localhost:3000, trang admin tại http://localhost:5173, đều gọi API qua gateway http://localhost:8080.
- Tài liệu thiết kế, API và sơ đồ nằm trong thư mục `docs/`.
