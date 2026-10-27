package com.example.producr_service.adapter.out.elasticsearch;

import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocumentFields;
import com.example.producr_service.adapter.mapper.ProductMapper;
import com.example.producr_service.adapter.mapper.ProductSearchDocumentMapper;
import com.example.producr_service.application.dto.request.ProductScrollFilter;
import com.example.producr_service.application.dto.request.ProductSortOption;
import com.example.producr_service.application.dto.request.ShopProductFilter;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.ProductCatalogSearchResult;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.port.out.ES.ProductSearchIndexPort;
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

    ProductMapper productMapper;

    @Override
    public void index(Product product) {
        ProductSearchDocument document =
                productSearchDocumentMapper.toDocument(product);

        productSearchRepository.save(document);
    }

    @Override
    public void deleteById(Long productId) {
        productSearchRepository.deleteById(productId);
    }

    @Override
    public ProductCatalogSearchResult search(ProductScrollFilter filter) {
        // Lấy dư một item để xác định còn batch kế tiếp hay không.
        Pageable pageable = PageRequest.of(
                0,
                filter.size() + 1
        );

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(boolQueryBuilder -> {
                    BoolQuery.Builder bool = buildProductQuery(filter.keyword(), filter.categoryId());
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

                    if (!filter.locations().isEmpty()) {
                        bool.filter(terms -> terms.terms(termsQuery -> termsQuery
                                .field(ProductSearchDocumentFields.LOCATION)
                                .terms(values -> values.value(filter.locations().stream()
                                        .map(FieldValue::of)
                                        .toList()))
                        ));
                    }

                    return boolQueryBuilder.bool(bool.build());
                });

        // Bộ lọc brand chỉ áp dụng cho hit để không thu hẹp danh sách brand gợi ý.
        if (!filter.brandIds().isEmpty()) {
            queryBuilder.withFilter(terms -> terms.terms(termsQuery -> termsQuery
                    .field(ProductSearchDocumentFields.BRAND_ID)
                    .terms(values -> values.value(filter.brandIds().stream()
                            .map(FieldValue::of)
                            .toList()))
            ));
        }

        applySort(queryBuilder, filter.sortOption() == ProductSortOption.MOST_SOLD
                ? ProductSearchDocumentFields.TOTAL_SOLD : "_score");

        // Lấy tối đa 50 brand có nhiều product khớp nhất, không phụ thuộc trang product hiện tại.
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
        // gửi data tiếp theo khi có cursor
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
        // backend luôn lấy dư 1 sản phẩm so với size fe gửi để check xem còn data để lấy k
        boolean hasNext = hits.size() > filter.size();
        // chỉ trả data = đúng size của filter
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

        return new ProductCatalogSearchResult(
                items,
                nextCursor,
                hasNext,
                extractBrandIds(searchHits)
        );
    }

    @Override
    public PageResponse<ProductSearchResponse> searchShopProducts(Long shopId, ShopProductFilter filter) {
        BoolQuery.Builder bool = buildProductQuery(filter.keyword(), filter.categoryId());
        bool.filter(term -> term.term(value -> value.field(ProductSearchDocumentFields.USER_ID).value(shopId)));
        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(query -> query.bool(bool.build()))
                .withTrackTotalHits(true)
                .withPageable(PageRequest.of(filter.pageQuery().pageIndex(), filter.pageQuery().size()));
        String sortField = switch (filter.sortOption()) {
            case RELEVANCE -> "_score";
            case NEWEST -> ProductSearchDocumentFields.CREATED_AT;
            case MOST_SOLD -> ProductSearchDocumentFields.TOTAL_SOLD;
        };
        applySort(queryBuilder, sortField);
        SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(
                queryBuilder.build(), ProductSearchDocument.class);
        List<ProductSearchResponse> items = hits.getSearchHits().stream()
                .map(SearchHit::getContent).map(productMapper::toResponse).toList();
        long totalItems = hits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalItems / filter.pageQuery().size());
        return new PageResponse<>(items, filter.pageQuery().page(), filter.pageQuery().size(), totalItems, totalPages);
    }

    private BoolQuery.Builder buildProductQuery(String keyword, Long categoryId) {
        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            bool.must(query -> query.multiMatch(match -> match.query(normalizedKeyword)
                    .type(TextQueryType.BoolPrefix).operator(Operator.And)
                    .fields(ProductSearchDocumentFields.NAME + "^5",
                            ProductSearchDocumentFields.BRAND_NAME + "^2", ProductSearchDocumentFields.CATEGORY_NAME)));
            bool.should(query -> query.matchPhrase(match -> match.field(ProductSearchDocumentFields.NAME)
                    .query(normalizedKeyword).boost(8.0f)));
        }
        if (categoryId != null) {
            bool.filter(query -> query.term(term -> term.field(ProductSearchDocumentFields.CATEGORY_ID).value(categoryId)));
        }
        bool.filter(query -> query.term(term -> term.field(ProductSearchDocumentFields.STATUS + ".keyword")
                .value(ProductStatus.ACTIVE.name())));
        return bool;
    }

    private void applySort(NativeQueryBuilder queryBuilder, String sortField) {
        queryBuilder.withSort(sort -> sort.field(field -> field.field(sortField).order(SortOrder.Desc)));
        // Dùng productId làm khóa phụ ổn định cho cursor và phân trang ở mọi chế độ sort.
        queryBuilder.withSort(sort -> sort.field(field -> field
                .field(ProductSearchDocumentFields.PRODUCT_ID).order(SortOrder.Desc)));
    }

    private List<Long> extractBrandIds(SearchHits<ProductSearchDocument> searchHits) {
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
                .map(bucket -> bucket.key())
                .toList();
    }

}
