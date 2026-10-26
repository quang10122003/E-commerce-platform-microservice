package com.example.producr_service.infrastructure.config;

import com.example.producr_service.adapter.out.embedding.JsoupHtmlTextExtractorAdapter;
import com.example.producr_service.adapter.out.embedding.GeminiEmbeddingAdapter;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.port.out.embedding.HtmlTextExtractorPort;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import com.example.producr_service.application.port.out.embedding.TextEmbeddingPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.service.ProductEmbeddingContentBuilderService;
import com.example.producr_service.application.service.ProductEmbeddingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class ProductEmbeddingConfig {
    @Bean
    ProductEmbeddingService productEmbeddingService(
            ProductEmbeddingRepositoryPort productEmbeddingRepositoryPort,
            TextEmbeddingPort textEmbeddingPort,
            ProductRepositoryPort productRepositoryPort,
            ProductEmbeddingContentBuilderService productEmbeddingContentBuilderService) {
        return new ProductEmbeddingService(productEmbeddingRepositoryPort, textEmbeddingPort,
                productRepositoryPort, productEmbeddingContentBuilderService);
    }

    @Bean
    TextEmbeddingPort textEmbeddingPort(
            RestClient restClient,
            @Value("${app.embedding.gemini.base-url}") String baseUrl,
            @Value("${app.embedding.gemini.model}") String model,
            @Value("${app.embedding.gemini.api-key}") String apiKey,
            @Value("${app.embedding.gemini.connect-timeout}") Duration connectTimeout,
            @Value("${app.embedding.gemini.read-timeout}") Duration readTimeout) {
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("Chưa cấu hình GEMINI_API_KEY");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        RestClient geminiClient = restClient.mutate()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        return new GeminiEmbeddingAdapter(geminiClient, model, Constant.PRODUCT_EMBEDDING_DIMENSIONS);
    }

    @Bean
    HtmlTextExtractorPort htmlTextExtractorPort() {
        return new JsoupHtmlTextExtractorAdapter();
    }

    @Bean
    ProductEmbeddingContentBuilderService productEmbeddingContentBuilderService(
            HtmlTextExtractorPort htmlTextExtractorPort) {
        return new ProductEmbeddingContentBuilderService(htmlTextExtractorPort);
    }
}
