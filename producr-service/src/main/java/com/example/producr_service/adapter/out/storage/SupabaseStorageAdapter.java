package com.example.producr_service.adapter.out.storage;

import com.example.producr_service.application.port.out.FileStoragePort;
import com.example.producr_service.domain.until.PreparedUpload;
import com.example.producr_service.domain.until.StorageBucket;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Slf4j
@Component
public class SupabaseStorageAdapter implements FileStoragePort {
    RestClient restClient;

    @Value("${supabase.url}")
    String supabaseUrl;

    @Value("${supabase.service-role-key}")
    String serviceRoleKey;



    public SupabaseStorageAdapter(
            RestClient restClient,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey
    ) {
        Assert.hasText(supabaseUrl, "Thiếu cấu hình SUPABASE_URL");
        Assert.hasText(serviceRoleKey, "Thiếu cấu hình SUPABASE_SERVICE_ROLE_KEY");

        this.restClient = restClient;
        // Kiểm tra và chuẩn hóa URL trước khi dùng để tạo endpoint upload.
        this.supabaseUrl = validateSupabaseUrl(supabaseUrl);
        this.serviceRoleKey = serviceRoleKey;
    }

    // Xác thực URL gốc của Supabase và loại bỏ dấu slash ở cuối.
    private String validateSupabaseUrl(String value) {
        String normalizedUrl = value.trim();

        if (normalizedUrl.endsWith("/")) {
            normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
        }

        URI uri;
        try {
            uri = URI.create(normalizedUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "SUPABASE_URL không phải URL hợp lệ",
                    exception
            );
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getHost() == null
                || uri.getPath() != null && !uri.getPath().isBlank()
                || normalizedUrl.contains("/storage/v1")) {
            throw new IllegalStateException(
                    "SUPABASE_URL phải là URL gốc dạng https://<project-ref>.supabase.co"
            );
        }

        return normalizedUrl;
    }


    @Override
    public String upload(PreparedUpload upload) {

        String bucketName = resolveBucket(upload.bucket());

        try {
            restClient.post()
                    .uri(this.supabaseUrl
                            + "/storage/v1/object/"
                            + bucketName
                            + "/"
                            + upload.objectPath())
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("x-upsert", "false")
                    .contentType(MediaType.parseMediaType(upload.contentType()))
                    .body(upload.content())
                    .retrieve()
                    .toBodilessEntity();

            log.info(
                    "Tải file {} lên bucket {} thành công",
                    upload.objectPath(),
                    bucketName
            );
            return this.supabaseUrl
                    + "/storage/v1/object/public/"
                    + bucketName
                    + "/"
                    + upload.objectPath();

        } catch (RestClientResponseException exception) {
            log.error(
                    "Tải file {} lên bucket {} thất bại",
                    upload.objectPath(),
                    bucketName
            );
            throw new IllegalStateException(
                    "Upload file lên Supabase thất bại: "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        }
    }

    // Gọi Storage API để xóa một hoặc nhiều object trong bucket.
    @Override
    public void delete(StorageBucket bucket, List<String> objectPaths) {
        String bucketName = resolveBucket(bucket);
        validateDeletePaths(objectPaths);

        try {
            restClient.method(HttpMethod.DELETE)
                    .uri(this.supabaseUrl
                            + "/storage/v1/object/"
                            + bucketName)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", objectPaths))
                    .retrieve()
                    .toBodilessEntity();

            log.info(
                    "Xóa file {} trong bucket {} thành công",
                    objectPaths,
                    bucketName
            );
        } catch (RestClientResponseException exception) {
            log.error(
                    "Xóa file {} trong bucket {} thất bại",
                    objectPaths,
                    bucketName
            );
            throw new IllegalStateException(
                    "Xóa file trên Supabase thất bại: "
                            + exception.getResponseBodyAsString(),
                    exception
            );
        }
    }

    // Kiểm tra danh sách object trước khi gửi request xóa hàng loạt.
    private void validateDeletePaths(List<String> objectPaths) {
        Assert.notEmpty(objectPaths, "Danh sách đường dẫn object không được để trống");
        Assert.isTrue(objectPaths.size() <= 1000, "Mỗi lần chỉ được xóa tối đa 1000 object");

        objectPaths.forEach(path -> Assert.hasText(
                path,
                "Đường dẫn object không được để trống"
        ));
    }

    // Chuyển enum nghiệp vụ thành tên bucket thực tế trên Supabase.
    private String resolveBucket(StorageBucket bucket) {
        return switch (bucket) {
            case CATEGORY -> "category";
            case PRODUCT_IMAGES -> "product-images";
            case PRODUCT_VARIANTS -> "product-variants";
        };
    }
}
