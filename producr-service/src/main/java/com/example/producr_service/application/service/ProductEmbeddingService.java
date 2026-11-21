package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.embedding.ProductEmbeddingContent;
import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import com.example.producr_service.application.port.out.embedding.HtmlTextExtractorPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.regex.Pattern;
import com.example.producr_service.application.port.out.embedding.TextEmbeddingPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class ProductEmbeddingService {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\p{Z}]+");

    private final ProductEmbeddingRepositoryPort productEmbeddingRepositoryPort;
    private final TextEmbeddingPort textEmbeddingPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final HtmlTextExtractorPort htmlTextExtractorPort;

    public void syncLatest(Long productId) {
        Optional<Product> product = productRepositoryPort.findById(productId);
        if (product.isEmpty()) {
            log.info("Bỏ qua đồng bộ embedding vì sản phẩm đã bị xóa productId={}", productId);
            return;
        }
        // Đọc trạng thái hiện tại để retry không dùng nội dung cũ chụp trong event.
        sync(buildContent(product.get()));
    }

    // lấy data và check điệu liện hash để call api tạo vector
    private void sync(ProductEmbeddingContent content) {
        boolean unchanged = productEmbeddingRepositoryPort
                .findContentHashByProductId(content.productId())
                .filter(content.contentHash()::equals)
                .isPresent();

        if (unchanged) {
            log.info("Bỏ qua tạo embedding vì nội dung không đổi productId={}", content.productId());
            return;
        }

        List<Float> vector = textEmbeddingPort.embed(content.text());
        ProductEmbeddingData embedding = new ProductEmbeddingData(
                content.productId(), vector, content.contentHash());
        productEmbeddingRepositoryPort.save(embedding);

        log.info("Đã lưu embedding sản phẩm productId={}, contentHash={}",
                content.productId(), content.contentHash());
    }

    private ProductEmbeddingContent buildContent(Product product) {
        String name = normalize(product.getName());
        String categoryName = normalize(product.getCategoryName());
        String brandName = normalize(product.getBrandName());
        String description = normalize(htmlTextExtractorPort.toPlainText(product.getDescription()));

        List<String> lines = new ArrayList<>();
        lines.add("Tên sản phẩm: " + name);
        lines.add("Danh mục: " + categoryName);
        if (!brandName.isEmpty()) {
            lines.add("Thương hiệu: " + brandName);
        }
        lines.add("Mô tả: " + description);

        String text = String.join("\n", lines);
        return new ProductEmbeddingContent(product.getId(), text, sha256(text));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFC);
        return WHITESPACE.matcher(normalized).replaceAll(" ").strip();
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Không thể tính SHA-256 cho nội dung embedding", exception);
        }
    }
}
