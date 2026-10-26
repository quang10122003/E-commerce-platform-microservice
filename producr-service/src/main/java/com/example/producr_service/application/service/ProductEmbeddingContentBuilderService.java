package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.embedding.ProductEmbeddingContent;
import com.example.producr_service.application.port.out.embedding.HtmlTextExtractorPort;
import com.example.producr_service.domain.model.Product;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ProductEmbeddingContentBuilderService {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\p{Z}]+");

    private final HtmlTextExtractorPort htmlTextExtractorPort;

    public ProductEmbeddingContent build(Product product) {
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
