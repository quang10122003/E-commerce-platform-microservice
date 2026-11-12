package com.example.order_service.adapter.out.storage;

import com.example.order_service.application.constant.Constant;
import com.example.order_service.application.port.out.storage.FileStoragePort;
import com.example.order_service.application.port.out.storage.StorageBucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SupabaseStorageAdapter implements FileStoragePort {
    private final RestClient restClient = RestClient.builder().build();
    private final String supabaseUrl;
    private final String serviceRoleKey;

    public SupabaseStorageAdapter(@Value("${supabase.url}") String supabaseUrl,
                                  @Value("${supabase.service-role-key}") String serviceRoleKey) {
        Assert.hasText(supabaseUrl, "Thiếu cấu hình SUPABASE_URL");
        Assert.hasText(serviceRoleKey, "Thiếu cấu hình SUPABASE_SERVICE_ROLE_SECRET_KEY");
        this.supabaseUrl = validateSupabaseUrl(supabaseUrl);
        this.serviceRoleKey = serviceRoleKey;
    }

    // Chỉ nhận URL gốc https của project để không gửi service key tới nơi khác.
    private static String validateSupabaseUrl(String value) {
        String normalizedUrl = value.trim().replaceAll("/+$", "");
        URI uri;
        try {
            uri = URI.create(normalizedUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("SUPABASE_URL không phải URL hợp lệ", exception);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getPath() != null && !uri.getPath().isBlank()) {
            throw new IllegalStateException("SUPABASE_URL phải là URL gốc dạng https://<project-ref>.supabase.co");
        }
        return normalizedUrl;
    }

    @Override
    public String upload(StorageBucket bucket, String objectPath, String contentType, byte[] content) {
        String bucketName = bucket.getBucketName();
        try {
            restClient.post()
                    .uri(supabaseUrl + Constant.STORAGE_OBJECT_PATH + bucketName + "/" + objectPath)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .header("x-upsert", "false")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Tải file {} lên bucket {} thành công", objectPath, bucketName);
            return supabaseUrl + Constant.STORAGE_PUBLIC_OBJECT_PATH + bucketName + "/" + objectPath;
        } catch (RestClientResponseException exception) {
            log.error("Tải file {} lên bucket {} thất bại: status={}", objectPath, bucketName,
                    exception.getStatusCode().value());
            throw new IllegalStateException("Upload file lên Supabase thất bại", exception);
        }
    }

    @Override
    public void delete(StorageBucket bucket, List<String> objectPaths) {
        Assert.notEmpty(objectPaths, "Danh sách đường dẫn object không được để trống");
        objectPaths.forEach((String path) -> Assert.hasText(path, "Đường dẫn object không được để trống"));
        String bucketName = bucket.getBucketName();
        try {
            restClient.method(HttpMethod.DELETE)
                    .uri(supabaseUrl + Constant.STORAGE_OBJECT_PATH + bucketName)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .header("apikey", serviceRoleKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", objectPaths))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Xóa file {} trong bucket {} thành công", objectPaths, bucketName);
        } catch (RestClientResponseException exception) {
            log.error("Xóa file {} trong bucket {} thất bại: status={}", objectPaths, bucketName,
                    exception.getStatusCode().value());
            throw new IllegalStateException("Xóa file trên Supabase thất bại", exception);
        }
    }
}
