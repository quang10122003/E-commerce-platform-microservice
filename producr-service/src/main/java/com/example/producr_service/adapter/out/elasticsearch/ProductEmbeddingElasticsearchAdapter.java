package com.example.producr_service.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductEmbeddingDocument;
import com.example.producr_service.adapter.mapper.ProductEmbeddingDocumentMapper;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductEmbeddingElasticsearchAdapter
        implements ProductEmbeddingRepositoryPort {

    private final ProductEmbeddingRepository repository;
    private final ProductEmbeddingDocumentMapper mapper;
    private final ElasticsearchClient elasticsearchClient;

    // lấy các sản phẩm có vector giống vector user nhất dựa vào  chỉ số Cosine bằng thuật toán Cosine Similarity.
    @Override
    public List<Long> findSimilarProductIds(List<Float> userVector, int limit) {
        if (userVector.isEmpty()) {
            return List.of();
        }
        if (limit <= 0 || userVector.size() != Constant.PRODUCT_EMBEDDING_DIMENSIONS
                || userVector.stream().anyMatch(value -> value == null || !Float.isFinite(value))
                || userVector.stream().noneMatch(value -> value != 0)) {
            throw new IllegalArgumentException("Vector hoặc giới hạn ứng viên không hợp lệ");
        }
        SearchRequest request = SearchRequest.of(builder -> builder
                .index(Constant.PRODUCT_EMBEDDING_INDEX)
                .size(limit)
                .query(query -> query.scriptScore(scriptScore -> scriptScore
                        .query(filter -> filter.exists(exists -> exists.field("vector")))
                        .script(script -> script
                                .source(source -> source.scriptString(
                                        "cosineSimilarity(params.userVector, 'vector') + 1.0"))
                                .params("userVector", JsonData.of(userVector)))))
                .source(source -> source.filter(filter -> filter.includes("productId"))));
        try {
            SearchResponse<ProductEmbeddingDocument> response = elasticsearchClient.search(
                    request, ProductEmbeddingDocument.class);
            if (response.timedOut() || response.shards().failed().intValue() > 0) {
                throw new IllegalStateException("Elasticsearch trả ứng viên không đầy đủ");
            }
            return response.hits().hits().stream().map(hit -> {
                ProductEmbeddingDocument document = hit.source();
                if (document == null || document.getProductId() == null) {
                    throw new IllegalStateException("Ứng viên embedding thiếu productId");
                }
                return document.getProductId();
            }).toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể tìm ứng viên theo vector", exception);
        }
    }

    @Override
    public void deleteById(Long productId) {
        repository.deleteById(productId);
    }

    @Override
    public Map<Long, ProductEmbeddingData> findAllByProductIds(Collection<Long> productIds) {
        Objects.requireNonNull(productIds, "Danh sách productId không được null");
        if (productIds.isEmpty()) {
            return Map.of();
        }
        if (productIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("ProductId phải là số dương");
        }

        List<String> documentIds = productIds.stream()
                .distinct()
                .map(String::valueOf)
                .toList();
        Map<Long, ProductEmbeddingData> embeddings = new HashMap<>();
        int batchSize = Constant.PRODUCT_EMBEDDING_READ_BATCH_SIZE;

        for (int offset = 0; offset < documentIds.size(); offset += batchSize) {
            List<String> batch = documentIds.subList(offset,
                    Math.min(offset + batchSize, documentIds.size()));
            for (ProductEmbeddingData embedding : findEmbeddingBatch(batch)) {
                embeddings.put(embedding.productId(), embedding);
            }
        }
        return Map.copyOf(embeddings);
    }

    private List<ProductEmbeddingData> findEmbeddingBatch(List<String> documentIds) {
        SearchRequest request = SearchRequest.of(builder -> builder
                .index(Constant.PRODUCT_EMBEDDING_INDEX)
                .size(documentIds.size())
                .query(query -> query.ids(ids -> ids.values(documentIds)))
                // ES có thể ẩn vector trong _source mặc định nên phải yêu cầu trả rõ ràng.
                .source(source -> source.filter(filter -> filter
                        .excludeVectors(false)
                        .includes("productId", "vector", "contentHash"))));

        try {
            SearchResponse<ProductEmbeddingDocument> response = elasticsearchClient.search(
                    request, ProductEmbeddingDocument.class);
            if (response.timedOut() || response.shards().failed().intValue() > 0) {
                throw new IllegalStateException("Elasticsearch trả kết quả embedding không đầy đủ");
            }
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .map(document -> {
                        if (document == null) {
                            throw new IllegalStateException("Elasticsearch không trả nội dung document embedding");
                        }
                        return mapper.toData(document);
                    })
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể đọc embedding sản phẩm từ Elasticsearch", exception);
        }
    }

    @Override
    public Optional<String> findContentHashByProductId(Long productId) {
        return repository.findById(productId)
                .map(ProductEmbeddingDocument::getContentHash);
    }

    @Override
    public void save(ProductEmbeddingData embedding) {
        if (embedding.vector().size()
                != Constant.PRODUCT_EMBEDDING_DIMENSIONS) {
            throw new IllegalArgumentException(
                    "Số chiều vector không khớp mapping Elasticsearch");
        }

        ProductEmbeddingDocument document = mapper.toDocument(embedding);
        repository.save(document);
    }
}
