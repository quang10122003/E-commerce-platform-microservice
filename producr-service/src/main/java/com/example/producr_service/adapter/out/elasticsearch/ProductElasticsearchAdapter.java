package com.example.producr_service.adapter.out.elasticsearch;

import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocumentFields;
import com.example.producr_service.adapter.mapper.ProductMapper;
import com.example.producr_service.adapter.mapper.ProductSearchDocumentMapper;
import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.request.ProductSortOption;
import com.example.producr_service.application.dto.response.ProductBrandOption;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResponse;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Objects;

import static com.example.common.untill.ValidationUtils.hasText;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ProductElasticsearchAdapter implements ProductSearchIndexPort {

    private static final String BRANDS_AGGREGATION = "matched_brands";

    ProductSearchDocumentMapper productSearchDocumentMapper;

    ProductSearchRepository productSearchRepository;

    ElasticsearchOperations elasticsearchOperations;

    ProductSearchCursorCodec productSearchCursorCodec;

    BrandRepositoryPort brandRepositoryPort;
    ProductMapper productMapper;

    @Override
    public void index(Product product, String location) {
        ProductSearchDocument document =
                productSearchDocumentMapper.toDocument(product, location);

        productSearchRepository.save(document);
    }

    @Override
    public void deleteById(Long productId) {
        productSearchRepository.deleteById(productId);
    }

    @Override
    public ProductCatalogSearchResponse search(ProductScrollFilter filter) {
        // Lấy dư một item để xác định còn batch kế tiếp hay không.
        Pageable pageable = PageRequest.of(
                0,
                filter.size() + 1
        );

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(boolQueryBuilder -> boolQueryBuilder.bool(bool -> {
                    if (hasText(filter.keyword())) {
                        String keyword = filter.keyword().trim();
                        bool.must(multiMatch -> multiMatch.multiMatch(multiMatchQuery -> multiMatchQuery
                                .query(keyword)
                                .type(TextQueryType.BoolPrefix)
                                .operator(Operator.And)
                                .fields(
                                        ProductSearchDocumentFields.NAME + "^5",
                                        ProductSearchDocumentFields.BRAND_NAME + "^2",
                                        ProductSearchDocumentFields.CATEGORY_NAME
                                )
                        ));

                        bool.should(matchPhrase -> matchPhrase.matchPhrase(matchPhraseQuery -> matchPhraseQuery
                                .field(ProductSearchDocumentFields.NAME)
                                .query(keyword)
                                .boost(8.0f)
                        ));
                    }

                    if (filter.categoryId() != null) {
                        bool.filter(term -> term.term(termQuery -> termQuery
                                .field(ProductSearchDocumentFields.CATEGORY_ID)
                                .value(filter.categoryId())
                        ));
                    }

                    if (filter.minPrice() != null) {
                        bool.filter(range -> range.range(rangeQuery -> rangeQuery
                                .number(numberRange -> numberRange
                                        .field(ProductSearchDocumentFields.MAX_PRICE)
                                        .gte(filter.minPrice().doubleValue())
                                )
                        ));
                    }

                    if (filter.maxPrice() != null) {
                        bool.filter(range -> range.range(rangeQuery -> rangeQuery
                                .number(numberRange -> numberRange
                                        .field(ProductSearchDocumentFields.MAX_PRICE)
                                        .lte(filter.maxPrice().doubleValue())
                                )
                        ));
                    }

                    bool.filter(term -> term.term(termQuery -> termQuery
                            .field(ProductSearchDocumentFields.STATUS + ".keyword")
                            .value(ProductStatus.ACTIVE.name())
                    ));

                    if (!filter.locations().isEmpty()) {
                        bool.filter(terms -> terms.terms(termsQuery -> termsQuery
                                .field(ProductSearchDocumentFields.LOCATION)
                                .terms(values -> values.value(filter.locations().stream()
                                        .map(FieldValue::of)
                                        .toList()))
                        ));
                    }

                    return bool;
                }))
                ;

        // Bộ lọc brand chỉ áp dụng cho hit để aggregation vẫn chứa đủ brand khớp tìm kiếm.
        if (!filter.brandIds().isEmpty()) {
            queryBuilder.withFilter(terms -> terms.terms(termsQuery -> termsQuery
                    .field(ProductSearchDocumentFields.BRAND_ID)
                    .terms(values -> values.value(filter.brandIds().stream()
                            .map(FieldValue::of)
                            .toList()))
            ));
        }

        if (filter.sortOption() == ProductSortOption.MOST_SOLD) {
            queryBuilder.withSort(sort -> sort.field(field -> field
                    .field(ProductSearchDocumentFields.TOTAL_SOLD)
                    .order(SortOrder.Desc)
            ));
        } else {
            queryBuilder.withSort(sort -> sort.field(field -> field
                    .field("_score")
                    .order(SortOrder.Desc)
            ));
        }

        // Dùng productId làm khóa phụ ổn định cho cursor ở mọi chế độ sort.
        queryBuilder.withSort(sort -> sort.field(field -> field
                .field(ProductSearchDocumentFields.PRODUCT_ID)
                .order(SortOrder.Desc)
        ));

        // Gom brandId trên toàn bộ tập product khớp để không phụ thuộc vào kích thước page.
        queryBuilder.withAggregation(
                BRANDS_AGGREGATION,
                Aggregation.of(aggregation -> aggregation.terms(terms -> terms
                        .field(ProductSearchDocumentFields.BRAND_ID)
                        .size(50)
                ))
        );

        NativeQuery query = queryBuilder
                .withPageable(pageable)
                .build();

        if (hasText(filter.cursor())) {
            query.setSearchAfter(productSearchCursorCodec.decode(
                    filter.cursor(),
                    filter.sortOption()
            ));
        }

        SearchHits<ProductSearchDocument> searchHits = elasticsearchOperations.search(
                query,
                ProductSearchDocument.class
        );

        List<SearchHit<ProductSearchDocument>> hits = searchHits.getSearchHits();
        boolean hasNext = hits.size() > filter.size();
        List<SearchHit<ProductSearchDocument>> visibleHits = hits.stream()
                .limit(filter.size())
                .toList();

        List<ProductSearchResponse> items = visibleHits.stream()
                .map(SearchHit::getContent)
                .map(productMapper::toResponse)
                .toList();

        String nextCursor = hasNext
                ? productSearchCursorCodec.encode(
                        visibleHits.getLast().getSortValues(),
                        filter.sortOption()
                )
                : null;

        return new ProductCatalogSearchResponse(
                items,
                nextCursor,
                hasNext,
                extractBrands(searchHits)
        );
    }

    private List<ProductBrandOption> extractBrands(SearchHits<ProductSearchDocument> searchHits) {
        if (!searchHits.hasAggregations()) {
            return List.of();
        }

        ElasticsearchAggregations aggregations =
                (ElasticsearchAggregations) searchHits.getAggregations();
        ElasticsearchAggregation aggregation = aggregations.get(BRANDS_AGGREGATION);
        if (aggregation == null || !aggregation.aggregation().getAggregate().isLterms()) {
            return List.of();
        }

        return aggregation.aggregation().getAggregate().lterms().buckets().array().stream()
                .map(bucket -> brandRepositoryPort.findById(bucket.key())
                        .map(brand -> new ProductBrandOption(brand.getId(), brand.getName()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

}
