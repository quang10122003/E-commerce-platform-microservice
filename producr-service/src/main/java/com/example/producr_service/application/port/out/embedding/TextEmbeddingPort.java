package com.example.producr_service.application.port.out.embedding;

import java.util.List;

public interface TextEmbeddingPort {
    // chuyển content thành vector
    List<Float> embed(String text);
}
