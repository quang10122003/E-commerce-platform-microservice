package com.example.producr_service.application.dto.embedding;


//DTO nội bộ lưu thông tin đc chuẩn hóa của 1 sảm phầm để tạo vector
public record ProductEmbeddingContent( Long productId,
                                       String text,
                                       String contentHash) {

}
