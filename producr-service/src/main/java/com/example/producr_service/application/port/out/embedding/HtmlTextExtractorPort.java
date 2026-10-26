package com.example.producr_service.application.port.out.embedding;

// port để chuẩn hóa thông tin html về text
public interface HtmlTextExtractorPort {
    String toPlainText(String html);
}
